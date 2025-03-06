package dev.lyphium.egghunt.inventory;

import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import dev.lyphium.egghunt.util.NamespacedKeyConstants;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class EasterEggInventory implements InventoryHolder {

    @Getter
    private final Inventory inventory;
    private final ResourceManager resourceManager;
    private final Locale locale;

    private int page;

    public EasterEggInventory(
            @NotNull ResourceManager resourceManager,
            @NotNull Locale locale
    ) {
        this.resourceManager = resourceManager;
        this.locale = locale;
        this.inventory = Bukkit.createInventory(this, 54, Component.translatable("inventory.eggs.title", ColorConstants.ERROR));

        fillInventory();
    }

    public void changePage(int page) {
        fillInventory();
    }

    private void fillInventory() {
        final List<ItemStack> eggs = resourceManager.getEggs();

        inventory.clear();
        for (int i = 0; i < eggs.size() && i < 54; i++) {
            final ItemStack item = eggs.get(i).clone();

            item.editMeta(meta -> {
                final List<Component> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.lore())) : new ArrayList<>();

                if (!lore.isEmpty())
                    lore.add(Component.empty());

                lore.add(GlobalTranslator.render(Component.translatable("inventory.eggs.delete", ColorConstants.DEFAULT, Component.keybind("key.drop"))
                        .decoration(TextDecoration.ITALIC, false), locale));
                meta.lore(lore);
            });

            inventory.setItem(i, item);
        }
    }

    public @Nullable ItemStack getEgg(int slot) {
        ItemStack item = inventory.getItem(slot);
        if (item == null)
            return null;

        item = item.clone();
        item.editMeta(meta -> {
            final PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(NamespacedKeyConstants.EASTER_EGG_KEY, PersistentDataType.BOOLEAN, true);

            final List<Component> lore = Objects.requireNonNull(meta.lore());
            final Component description = Component.translatable("tutorial.socialInteractions.description", ColorConstants.DEFAULT,
                            Component.keybind("key.sneak").append(Component.text("+")).append(Component.keybind("key.mouse.left")))
                    .decoration(TextDecoration.ITALIC, false);

            lore.set(lore.size() - 1, description);
            meta.lore(lore);
        });

        return item;
    }

    public void addEgg(@NotNull ItemStack item) {
        item = item.asOne();
        item.editMeta(meta -> {
            final PersistentDataContainer container = meta.getPersistentDataContainer();
            container.remove(NamespacedKeyConstants.EASTER_EGG_KEY);

            final List<Component> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.lore())) : new ArrayList<>();
            final Component description = Component.translatable("tutorial.socialInteractions.description", ColorConstants.DEFAULT,
                            Component.keybind("key.sneak").append(Component.text("+")).append(Component.keybind("key.mouse.left")))
                    .decoration(TextDecoration.ITALIC, false);

            lore.remove(description);
            if (!lore.isEmpty() && lore.getLast().equals(Component.empty()))
                lore.removeLast();

            meta.lore(lore);
        });

        resourceManager.getEggs().add(item);
        resourceManager.saveResources();

        fillInventory();
    }

    public @Nullable ItemStack removeEgg(int slot) {
        final ItemStack item = inventory.getItem(slot);
        if (item == null)
            return null;

        item.editMeta(meta -> {
            final List<Component> lore = Objects.requireNonNull(meta.lore());
            lore.removeLast();
            if (!lore.isEmpty())
                lore.removeLast();

            meta.lore(lore);
        });

        resourceManager.getEggs().remove(item);
        resourceManager.saveResources();

        fillInventory();
        return item;
    }
}
