package org.teameugene.prison.Util;

import org.bukkit.NamespacedKey;
import org.teameugene.prison.Prison;

public class Keys {
    public static NamespacedKey positionKey;
    public static NamespacedKey radiusKey;

    public Keys() {
        positionKey = new NamespacedKey(Prison.getInstance(), "pos");
        radiusKey = new NamespacedKey(Prison.getInstance(), "rad");
    }
}
