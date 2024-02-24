package org.teameugene.prison.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.teameugene.prison.Prison;

public class SoundSystem {
    public static BukkitTask playContinuousSound(final Location location, final Sound sound, final float volume, final float pitch, final Player player) {
        // Create and return the BukkitTask representing the scheduled task
        return new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(location, sound, volume, pitch);
            }
        }.runTaskTimerAsynchronously(Prison.getInstance(), 0L, 20 * 10L); // Delay 0 ticks, period 20 ticks (1 second)
    }

    public static void stopSound(Player player, Sound sound) {
        player.stopSound(sound);
    }
}
