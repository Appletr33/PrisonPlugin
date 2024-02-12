package org.teameugene.prison.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.teameugene.prison.Prison;

public class CommandListener implements Listener {
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        if (event.getCommand().equalsIgnoreCase("reload confirm")) {
            Prison.getInstance().reloaded = true;
        }
    }
}
