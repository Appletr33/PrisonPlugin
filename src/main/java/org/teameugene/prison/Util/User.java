package org.teameugene.prison.Util;

import org.bukkit.entity.Player;

import java.util.UUID;

public class User {
    private long points;
    private Player player;

    public User(long points, Player player) {
        this.points = points;
        this.player = player;
    }

    public long getPoints() {
        return this.points;
    }

    public void addPoints(long pointsToAdd) {
        this.points += pointsToAdd;
    }

    public boolean subtractPoints(long pointsToSubtract) {
        if (this.points - pointsToSubtract >= 0.0) {
            this.points -= pointsToSubtract;
            return true;
        }
        return false;
    }

    public String getUserName() {
        return this.player.getName();
    }

    public UUID getUUID() {
        return this.player.getUniqueId();
    }

    public Player getPlayer() {
        return this.player;
    }

}
