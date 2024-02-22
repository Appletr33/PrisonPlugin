package org.teameugene.prison.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.teameugene.prison.Util.CustomMaps;
import org.teameugene.prison.ship.Radar;

public class ItemFrameListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
            ItemStack itemStack = itemFrame.getItem();

            if (itemStack != null && itemStack.getType() == Material.FILLED_MAP) {
                MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
                int mapId = mapMeta.getMapId();
                Player player = event.getPlayer();
                String mapName = CustomMaps.getMapNameFromId(mapId);

                if (mapName.equals("control_panel")) {
                    Radar.openGUI(player);
                }
            }
        }
    }
}