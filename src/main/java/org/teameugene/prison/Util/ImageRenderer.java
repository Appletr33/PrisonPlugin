package org.teameugene.prison.Util;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.Prison;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageRenderer extends MapRenderer {

    private boolean done;
    BufferedImage image;

    public ImageRenderer(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (done)
            return;

        canvas.drawImage(0, 0, image);
        map.setTrackingPosition(false);
        done = true;
    }
}
