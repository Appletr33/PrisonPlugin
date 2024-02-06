package org.teameugene.prison.worlds.mars;

import appl3.test.entities.CrazySnowman;
import appl3.test.entities.DeathCallback;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.teameugene.prison.tasks.Tasks;

import static org.teameugene.prison.mine.Utils.getWorldByName;


public class Mars {
    boolean monsterActive = false;
    private long lastSpawnTime = System.currentTimeMillis() - 10000;

    public Mars(Plugin plugin) {
        new spawnMonster().runTaskTimer(plugin, 0, 20 * 60);
    }

    DeathCallback myDeathCallback = new DeathCallback() {
        @Override
        public void onDeath() {
            monsterActive = false;
        }
    };


    private class spawnMonster extends BukkitRunnable {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if (!monsterActive && (currentTime - lastSpawnTime >= 10000)) {
                //Spawn bozo
                CrazySnowman.spawn(new Location(getWorldByName("mars"), -162, 29, 160), myDeathCallback);
                lastSpawnTime = currentTime;
                monsterActive = true;
            }
        }
    }
}
