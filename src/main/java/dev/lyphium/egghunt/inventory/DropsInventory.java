package dev.lyphium.egghunt.inventory;

import dev.lyphium.egghunt.data.EasterEggDrop;
import dev.lyphium.egghunt.manager.ResourceManager;
import dev.lyphium.egghunt.util.ColorConstants;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class DropsInventory implements InventoryHolder {

    @Getter
    private final Inventory inventory;
    private final ResourceManager resourceManager;
    private final Locale locale;

    public DropsInventory(@NotNull ResourceManager resourceManager, @NotNull Locale locale) {
        this.resourceManager = resourceManager;
        this.locale = locale;
        this.inventory = Bukkit.createInventory(this, 54, Component.translatable("inventory.drops.title", ColorConstants.ERROR));

        fillInventory();
    }

    private void fillInventory() {
        final List<EasterEggDrop> drops = resourceManager.getDrops();

        // TODO Handle more drops
        for (int i = 0; i < drops.size() && i < 54; i++) {
            final EasterEggDrop drop = drops.get(i);
            if (drop.getItemDrop() != null) {
                final ItemStack item = drop.getItemDrop().asOne();

                item.editMeta(meta -> {
                    final List<Component> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.lore())) : new ArrayList<>();
                    if (!lore.isEmpty())
                        lore.add(Component.empty());

                    lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.amount", ColorConstants.DEFAULT,
                                    Component.text(drop.getMinimumAmount(), ColorConstants.ERROR), Component.text(drop.getMaximumAmount(), ColorConstants.ERROR))
                            .decoration(TextDecoration.ITALIC, false), locale));
                    lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.weight", ColorConstants.DEFAULT,
                                    Component.text(drop.getWeight(), ColorConstants.ERROR))
                            .decoration(TextDecoration.ITALIC, false), locale));
                    meta.lore(lore);
                });

                inventory.setItem(i, item);
            } else if (drop.getCommandDrop() != null) {
                final ItemStack item = new ItemStack(Material.PAPER);
                item.editMeta(meta -> {
                    meta.displayName(Component.translatable("advMode.command", ColorConstants.HIGHLIGHT).decoration(TextDecoration.ITALIC, false));

                    final List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("» ", ColorConstants.DEFAULT).decoration(TextDecoration.ITALIC, false)
                            .append(Component.translatable(drop.getCommandDrop(), ColorConstants.WHITE).decoration(TextDecoration.ITALIC, false)));
                    lore.add(Component.empty());
                    lore.add(GlobalTranslator.render(Component.translatable("inventory.drops.weight", ColorConstants.DEFAULT,
                                    Component.text(drop.getWeight(), ColorConstants.ERROR))
                            .decoration(TextDecoration.ITALIC, false), locale));
                    meta.lore(lore);
                });

                inventory.setItem(i, item);
            }
        }
    }

    public @Nullable ItemStack removeDrop(int slot) {
        return null;
    }

    public void addDrop(@NotNull EasterEggDrop drop) {

    }
}
