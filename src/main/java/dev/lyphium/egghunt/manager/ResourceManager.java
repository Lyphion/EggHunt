package dev.lyphium.egghunt.manager;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class ResourceManager {

    private final List<Material> validBlocks = new ArrayList<>();

    private final List<ItemStack> eggs = new ArrayList<>();

    private final List<ItemStack> itemDrops = new ArrayList<>();

    private final List<String> commandDrops = new ArrayList<>();

    private Sound spawnSound, pickupSound, vanishSound;

    private double range;

    public void loadResources() {

    }

    public void saveResources() {

    }
}
