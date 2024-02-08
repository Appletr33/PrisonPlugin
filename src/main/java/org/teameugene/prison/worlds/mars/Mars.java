package org.teameugene.prison.worlds.mars;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static org.teameugene.prison.Util.Utils.getWorldByName;


public class Mars {
    boolean monsterActive = false;
    private long lastSpawnTime = System.currentTimeMillis() - 10000;

    public Mars(Plugin plugin) {
        new spawnMonster().runTaskTimer(plugin, 0, 20 * 60);
    }

//    DeathCallback myDeathCallback = new DeathCallback() {
//        @Override
//        public void onDeath() {
//            monsterActive = false;
//        }
//    };


    private class spawnMonster extends BukkitRunnable {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if (!monsterActive && (currentTime - lastSpawnTime >= 10000)) {
                //Spawn bozo
                //CrazySnowman.spawn(new Location(getWorldByName("mars"), -162, 29, 160), myDeathCallback);
                lastSpawnTime = currentTime;
                monsterActive = true;
            }
        }
    }
}
