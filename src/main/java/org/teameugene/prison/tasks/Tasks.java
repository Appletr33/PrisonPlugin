package org.teameugene.prison.tasks;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;

import java.util.ArrayList;

import static org.teameugene.prison.Util.Utils.updateDatabase;
import static org.teameugene.prison.scoreboard.ScoreBoard.displayScoreboard;

public class Tasks {

    Database database;
    ArrayList<User> connectedPlayers;

    public Tasks(Plugin plugin, Database database, ArrayList<User> connectedPlayers) {
        this.database = database;
        this.connectedPlayers = connectedPlayers;

        // Update score boards
        new updateScoreBoards().runTaskTimer(plugin, 0, 10); // every 10 ticks
        new updateDatabasePlayerScores().runTaskTimer(plugin, 0, 20 * 30); // every 20 * 3 ticks (3 seconds)
    }

    private class updateScoreBoards extends BukkitRunnable {
        @Override
        public void run() {
            if (database.isConnected())
                for (User user : connectedPlayers) {
                    displayScoreboard(user.getPlayer(), user.getPoints());
                }
        }
    }

    private class updateDatabasePlayerScores extends BukkitRunnable {
        @Override
        public void run() {
            updateDatabase(database, connectedPlayers);
        }
    }


}
