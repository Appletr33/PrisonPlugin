package org.teameugene.prison.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.teameugene.prison.Prison;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Jetpack implements Listener {

    private static Map<UUID, Boolean> gravityEnabled = new HashMap<>();

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (gravityEnabled.containsKey(player.getUniqueId())) {
            if (event.isSneaking()) {
                player.setVelocity(player.getVelocity().add(new Vector(0, -0.025, 0)));
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 0.2f, 1f);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (gravityEnabled.containsKey(player.getUniqueId())) {
            if (event.isSprinting()) {
                player.setVelocity(player.getLocation().getDirection().multiply(0.03));
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 0.2f, 1f);
            }
        }
    }

    public static void enableGravity(Player player) {
        if (!gravityEnabled.containsKey(player.getUniqueId())) {
            gravityEnabled.put(player.getUniqueId(), true);
            player.sendMessage("§aJetpack Activated");
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!gravityEnabled.containsKey(player.getUniqueId())) {
                        cancel();
                        return;
                    }
                    if (player.getLocation().getBlock().getType().isAir() &&
                            player.getLocation().subtract(0, 1, 0).getBlock().getType().isAir()) {
                        player.setVelocity(player.getVelocity().add(new Vector(0, 0.078, 0)));
                        if (player.isSneaking()) {
                            player.setVelocity(player.getVelocity().add(new Vector(0, -0.025, 0)));
                            player.playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 0.2f, 1f);
                        } else if (!player.isOnGround()) {
                            if (player.isSprinting()) {
                                player.setVelocity(player.getLocation().getDirection().multiply(0.03));
                                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 0.2f, 1f);
                            }
                        }
                    }
                }
            }.runTaskTimer(Prison.getInstance(), 0L, 1L);
        }
    }

    public static void disableGravity(Player player) {
        gravityEnabled.remove(player.getUniqueId());
    }
}
