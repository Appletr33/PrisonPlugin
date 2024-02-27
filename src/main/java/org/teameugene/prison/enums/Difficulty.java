package org.teameugene.prison.enums;

import org.bukkit.ChatColor;

public enum Difficulty {
    Peaceful(ChatColor.GREEN),
    Dangerous(ChatColor.YELLOW),
    Extreme(ChatColor.RED),
    Deadly(ChatColor.DARK_RED);

    private final ChatColor chatColor;

    Difficulty(ChatColor chatColor) {
        this.chatColor = chatColor;
    }


    public ChatColor getChatColor() {
        return chatColor;
    }
}
