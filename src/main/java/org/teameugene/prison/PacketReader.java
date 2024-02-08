package org.teameugene.prison;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.teameugene.prison.events.RightClickNPC;
import org.teameugene.prison.npcs.NPC;

import java.lang.reflect.Field;
import java.util.List;

import static org.teameugene.prison.Util.Utils.getPrivateValue;


public class PacketReader {

    private final Player player;
    private int count = 0;
    private Field connectionField;

    public PacketReader(Player player) {
        this.player = player;
    }

    public boolean inject() {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        Channel channel = ((Connection) getValue(craftPlayer.getHandle().connection, "c")).channel;
        ChannelPipeline pipeline = channel.pipeline();
        if (channel.pipeline() == null || pipeline.get("PacketInjector") != null)
            return false;

        pipeline.addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<ServerboundInteractPacket>() {

            @Override
            protected void decode(ChannelHandlerContext ctx, ServerboundInteractPacket msg, List<Object> out) throws Exception {
                out.add(msg);
                read(msg);
            }
        });
        return true;
    }

    private void read(ServerboundInteractPacket packet) {
        count++;
        if (count == 4) {
            count = 0;
            try {
                int entityID = (int) getPrivateValue(packet, "a"); //a value is the entity's id
                for (NPC npc : NPC.getNpcs()) {
                    if (entityID == npc.getNpcId()) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.getPluginManager().callEvent(new RightClickNPC(player, npc));
                            }
                        }.runTask(Prison.getInstance());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Object getValue(Object packet, String fieldName) {
        try {
            Field field = packet.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(packet);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
