package dev.lyphium.egghunt.util;

import org.bukkit.NamespacedKey;

public final class NamespacedKeyConstants {

    private static final String NAMESPACE = "egghunt";

    public static final NamespacedKey DROP_ID_KEY = new NamespacedKey(NAMESPACE, "drop_id");
    public static final NamespacedKey COMMAND_DROP_KEY = new NamespacedKey(NAMESPACE, "command_drop");

    public static final NamespacedKey EASTER_EGG_KEY = new NamespacedKey(NAMESPACE, "easter_egg");
    public static final NamespacedKey NATURAL_EGG_KEY = new NamespacedKey(NAMESPACE, "natural_egg");
    public static final NamespacedKey FAKE_EGG_KEY = new NamespacedKey(NAMESPACE, "fake_egg");
    public static final NamespacedKey BREAKABLE_EGG_KEY = new NamespacedKey(NAMESPACE, "breakable_egg");

}
