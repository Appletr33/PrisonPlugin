package org.teameugene.prison.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.teameugene.prison.events.RightClickNPC;

public class NPCInteractListener implements Listener {
    @EventHandler
    public void onInteract(RightClickNPC event) {
        event.getClickedNPC().interact(event.getPlayer());
    }
}
