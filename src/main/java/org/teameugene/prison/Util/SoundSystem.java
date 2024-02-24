package org.teameugene.prison.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.teameugene.prison.Prison;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SoundSystem {

    private Map<Sound, BukkitTask> continuouslyPlayingSounds = new HashMap<>();

    public void playContinuousSound(final Location location, final Sound sound, final float volume, final float pitch, long lengthInSeconds, final Player player, GameObject gameObject, int radiusToPlayTo) {
        // Create and return the BukkitTask representing the scheduled task

        boolean inRegion =  Utils.isInRegion(player.getLocation(), location.clone().subtract(radiusToPlayTo, radiusToPlayTo, radiusToPlayTo), location.clone().add(radiusToPlayTo, radiusToPlayTo, radiusToPlayTo));

        if ((continuouslyPlayingSounds.containsKey(sound) && !inRegion) || !gameObject.playingSound) {
            continuouslyPlayingSounds.get(sound).cancel();
            continuouslyPlayingSounds.remove(sound);
            stopSound(player, sound);
            return;
        }

        if (continuouslyPlayingSounds.containsKey(sound) || !inRegion)
            return;

        BukkitTask bt = new BukkitRunnable() {
            @Override
            public void run() {

                Prison.getInstance().getLogger().info("PLAYING = " + gameObject.playingSound);

                if (!gameObject.playingSound || !Utils.isInRegion(player.getLocation(), location.clone().subtract(radiusToPlayTo, radiusToPlayTo, radiusToPlayTo), location.clone().add(radiusToPlayTo, radiusToPlayTo, radiusToPlayTo))) {
                    if (continuouslyPlayingSounds.containsKey(sound)) {
                        continuouslyPlayingSounds.remove(sound);
                        stopSound(player, sound);
                    }
                    cancel();
                    return;
                }
                player.playSound(location, sound, volume, pitch);
            }
        }.runTaskTimerAsynchronously(Prison.getInstance(), 0L, 20 * lengthInSeconds); // Delay 0 ticks, period 20 ticks (1 second)

        continuouslyPlayingSounds.put(sound, bt);
    }

    public void playSound(Player player, Sound soundToPlay, Location locationOfSound, final float volume, final float pitch) {
        player.playSound(locationOfSound, soundToPlay, volume, pitch);
    }

    public void stopSound(Player player, Sound sound) {
        player.stopSound(sound);

        Iterator<Sound> iterator = continuouslyPlayingSounds.keySet().iterator();
        while (iterator.hasNext()) {
            Sound sd = iterator.next();
            if (sd.equals(sound)) {
                continuouslyPlayingSounds.get(sd).cancel();
                iterator.remove();
            }
        }
    }
}
