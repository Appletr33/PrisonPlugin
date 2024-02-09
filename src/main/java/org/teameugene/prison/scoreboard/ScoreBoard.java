package org.teameugene.prison.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreBoard {

    private static String buffer = "   ";

    public static void displayScoreboard(Player player, long points) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("customObjective", "dummy",
                ChatColor.BOLD + "Prisons");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Set username and score
        Score playerNameScore = objective.getScore(  ChatColor.RED + "" + ChatColor.BOLD + player.getName() + buffer);
        playerNameScore.setScore(4);

        Score spaceBuffer = objective.getScore(" " + buffer);
        spaceBuffer.setScore(3);

        // Set integer value (replace 0 with your desired value)
        Score intValueScore = objective.getScore(ChatColor.BOLD + "Credits " + ChatColor.RESET + ChatColor.GOLD + points + buffer);
        intValueScore.setScore(2);

        Score oxygenScore = objective.getScore(ChatColor.BOLD + "Oxygen " + ChatColor.RESET + ChatColor.GREEN + "100%" + buffer);
        oxygenScore.setScore(1);

        // Display scoreboard to the player
        player.setScoreboard(scoreboard);
    }
}
