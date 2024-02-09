package org.teameugene.prison.npcs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Rotation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
import org.teameugene.prison.Prison;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.teameugene.prison.Util.Utils.*;

public abstract class NPC {
    private static final ArrayList<NPC> npcs = new ArrayList<>();
    ServerPlayer npc;
    GameProfile gameProfile;
    ServerLevel serverLevel;
    MinecraftServer minecraftServer;
    Location location;
    int pitch = 0;
    int yaw = 180;
    int npcId;
    String name;

    public NPC(String name, Location location, String skinName) {
        if (name.length() < 16) {
            this.name = name;
            this.location = location;
            minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
            serverLevel = ((CraftWorld) location.getWorld()).getHandle();

            gameProfile = new GameProfile(UUID.randomUUID(), name);

            try {
                setSkin(skinName, gameProfile);
            } catch (Exception e) {
                e.printStackTrace();
            }


            npc = new ServerPlayer(minecraftServer, serverLevel, gameProfile, ClientInformation.createDefault());
            npc.setPos(location.getX(), location.getY(), location.getZ());
            npcId = npc.getId();
        } else {
            Prison.getInstance().getLogger().info("[ERROR] NAME TO LONG FOR [NPC] " + name + ". Failed to create NPC. Name must be less than or equal to 16 characters");
        }
    }

    public void setRotation(int pitch, int yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public abstract void interact(Player player);

    public void sendMessage(Player player, ChatColor nameColor, String message) {
        player.sendMessage(nameColor + "<" + name + ">" + " " + ChatColor.WHITE + message);
    }

    public static void showNPCS(Player player) {
        for (NPC npcPlayer : npcs) {
            if (npcPlayer.getWorld() == player.getWorld()) {
                ServerPlayer npc = npcPlayer.npc;
                SynchedEntityData synchedEntityData = npc.getEntityData();
                synchedEntityData.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 127);
                setValue(npc, "c", ((CraftPlayer) player).getHandle().connection);
                sendPacket(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc), player);
                sendPacket(new ClientboundAddEntityPacket(npc), player);
                sendPacket(new ClientboundSetEntityDataPacket(npc.getId(), synchedEntityData.getNonDefaultValues()), player);
                sendPacket(new ClientboundMoveEntityPacket.PosRot(npc.getId(), (short) 0, (short) 0, (short) 0, (byte) ((npcPlayer.yaw % 360.) * 256 / 360), (byte) ((npcPlayer.pitch % 360.) * 256 / 360), false), player);

                Bukkit.getScheduler().scheduleSyncDelayedTask(Prison.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        sendPacket(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(npc.getUUID())), player);
                    }
                }, 20);
            }
        }
    }

    public World getWorld() {
        return location.getWorld();
    }

    public static void setupNPCS() {
        // Moon NPCS //
        MoonMerchant moonMerchant = new MoonMerchant("Moon Merchant", new Location(getWorldByName("world"), -1791, 30, 795), "hijl");
        moonMerchant.setRotation(0, 90);

        WeaponForger weaponForger = new WeaponForger("Weapon Forger", new Location(getWorldByName("world"), -1791, 30, 796), "appl3");
        weaponForger.setRotation(0, 90);

        ArmorSmith armorSmith = new ArmorSmith("Armor Smith", new Location(getWorldByName("world"), -1791, 30, 797), "appl3");
        armorSmith.setRotation(0, 90);

        ShipMechanic shipMechanic = new ShipMechanic("Ship Mechanic", new Location(getWorldByName(Prison.moonWorldName), -1794, 30, 796), "appl3");

        npcs.add(moonMerchant);
        npcs.add(weaponForger);
        npcs.add(armorSmith);
        npcs.add(shipMechanic);
    }

    private void setSkin(String name, GameProfile gameProfile) {
        Gson gson = new Gson();
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        String json = getStringFromURL(url);
        String uuid = gson.fromJson(json, JsonObject.class).get("id").getAsString();

        url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
        json = getStringFromURL(url);
        JsonObject mainObject = gson.fromJson(json, JsonObject.class);
        JsonObject jsonObject = mainObject.get("properties").getAsJsonArray().get(0).getAsJsonObject();
        String value = jsonObject.get("value").getAsString();
        String signature = jsonObject.get("signature").getAsString();
        PropertyMap propertyMap = gameProfile.getProperties();
        //propertyMap.put("name", new Property("name", name));
        propertyMap.put("textures", new Property("textures", value, signature));
    }

    private void changeSkin(String value, String signature, GameProfile gameProfile) {
        gameProfile.getProperties().removeAll("textures");
        gameProfile.getProperties().put("textures", new Property("textures", value, signature));
    }

    public static ArrayList<NPC> getNpcs() {
        return npcs;
    }

    public int getNpcId() {
        return npcId;
    }
}
