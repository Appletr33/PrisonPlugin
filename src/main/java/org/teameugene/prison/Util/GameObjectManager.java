package org.teameugene.prison.Util;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.teameugene.prison.Prison;
import org.teameugene.prison.ship.Radar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameObjectManager {
    public static ArrayList<GameObject> gameObjects = new ArrayList<>();
    public static Map<String, Radar> radars = new HashMap<>();
    private static GameObjectManager instance;

    public GameObjectManager() {
        instance = this;
        for (Object obj : Serialize.instances) {
            if (obj instanceof GameObject object) {
                object.Initialize();
                gameObjects.add(object);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(Prison.getInstance(), 0, 10);
    }

    private void tick() {
        for (GameObject gameObject : gameObjects) {
            gameObject.tick();
        }
    }

    public static GameObjectManager getInstance() {
        return instance;
    }
}
