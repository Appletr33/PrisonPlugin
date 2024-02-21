package org.teameugene.prison.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapView;
import org.teameugene.prison.Util.ImageRenderer;

import static org.teameugene.prison.Util.CustomMaps.getImage;
import static org.teameugene.prison.Util.CustomMaps.hasImage;

public class MapListener implements Listener {

    @EventHandler
    public void onMapInitEvent(MapInitializeEvent event) {
        if (hasImage(event.getMap().getId())) {
            MapView view = event.getMap();
            view.getRenderers().clear();
            view.addRenderer(new ImageRenderer(getImage(view.getId())));
            view.setScale(MapView.Scale.FARTHEST);
            view.setTrackingPosition(false);
        }
    }
}
