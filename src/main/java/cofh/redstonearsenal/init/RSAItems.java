package cofh.redstonearsenal.init;

import cofh.core.item.ItemCoFH;
import cofh.core.item.ItemTierCoFH;
import cofh.redstonearsenal.item.*;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.item.crafting.Ingredient;

import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;

public class RSAItems {

    private RSAItems() {

    }

    public static void register() {

        ItemGroup combat = ItemGroup.TAB_COMBAT;
        ItemGroup tools = ItemGroup.TAB_TOOLS;
        ItemGroup misc = ItemGroup.TAB_MISC;

        Rarity rarity = Rarity.UNCOMMON;

        int energy = 320000;
        int xfer = 4000;

        // ITEMS.register("flux_metal_block", () -> new BlockItemCoFH(BLOCKS.get("flux_metal_block"), new Item.Properties().tab(misc)));

        ITEMS.register("flux_gem", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register("flux_ingot", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register("flux_nugget", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register("flux_dust", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register("flux_gear", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        // ITEMS.register("flux_plate", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        // ITEMS.register("flux_coin", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));

        ITEMS.register("flux_sword", () -> new FluxSwordItem(MATERIAL_FLUX_METAL, 3, -2.4F, new Item.Properties().tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_shovel", () -> new FluxShovelItem(MATERIAL_FLUX_METAL, 1.5F, -3.0F, new Item.Properties().tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_pickaxe", () -> new FluxPickaxeItem(MATERIAL_FLUX_METAL, 1, -2.8F, new Item.Properties().tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_axe", () -> new FluxAxeItem(MATERIAL_FLUX_METAL, 6.0F, -3.0F, new Item.Properties().tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_hoe", () -> new FluxHoeItem(MATERIAL_FLUX_METAL, -1.0F, new Item.Properties().tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
    }

    public static final IItemTier MATERIAL_FLUX_METAL = new ItemTierCoFH(4, 0, 8.0F, 3.0F, 18, () -> Ingredient.EMPTY);

}
