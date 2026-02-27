package dev.lyphium.egghunt.data;

import lombok.Getter;

@Getter
public enum EntityMode {

    ITEM("Item"),
    ARMOR_STAND("ArmorStand");

    private final String name;

    EntityMode(String name) {
        this.name = name;
    }

    public static EntityMode fromName(String name) {
        for (final EntityMode e : EntityMode.values()) {
            if (e.name.equalsIgnoreCase(name)) {
                return e;
            }
        }

        throw new IllegalArgumentException("Unknown EntityMode: " + name);
    }
}
