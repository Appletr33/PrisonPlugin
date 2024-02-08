package org.teameugene.prison.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.teameugene.prison.npcs.NPC;

public class RightClickNPC extends Event {
    private final Player player;
    private final NPC clickedNPC;

    private static final HandlerList HANDLERS = new HandlerList();

    public RightClickNPC(Player player, NPC clickedNPC) {
        this.player = player;
        this.clickedNPC = clickedNPC;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public NPC getClickedNPC() {
        return clickedNPC;
    }
}
