package org.teameugene.prison.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreBoard {

    private static String buffer = "     ";

    public static void displayScoreboard(Player player, long points) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("customObjective", "dummy",
                "Prisons");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Set username and score
        Score playerNameScore = objective.getScore(player.getName() + buffer);
        playerNameScore.setScore(1);

        // Set integer value (replace 0 with your desired value)
        Score intValueScore = objective.getScore("points: " + points + buffer);
        intValueScore.setScore(0);

        // Display scoreboard to the player
        player.setScoreboard(scoreboard);
    }
}
