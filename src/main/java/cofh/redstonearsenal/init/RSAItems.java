package cofh.redstonearsenal.init;

import cofh.core.item.ItemCoFH;
import cofh.lib.item.ArmorMaterialCoFH;
import cofh.lib.item.ItemTierCoFH;
import cofh.redstonearsenal.client.renderer.FluxTridentISTER;
import cofh.redstonearsenal.item.*;
import cofh.redstonearsenal.item.FluxElytraControllerItem;
import cofh.redstonearsenal.item.FluxElytraItem;
import cofh.redstonearsenal.item.FluxShieldItem;
import cofh.redstonearsenal.item.FluxSwordItem;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvents;

import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;
import static cofh.redstonearsenal.init.RSAIDs.*;

public class RSAItems {

    private RSAItems() {

    }

    public static void register() {

        ItemGroup combat = ItemGroup.TAB_COMBAT;
        ItemGroup transportation = ItemGroup.TAB_TRANSPORTATION;
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

        ITEMS.register("flux_sword", () -> new FluxSwordItem(MATERIAL_FLUX_METAL, 4, -2.4F, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_shield", () -> new FluxShieldItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_bow", () -> new FluxBowItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), 1, 1, 1, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_crossbow", () -> new FluxCrossbowItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), 1, 1, 1, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_quiver", () -> new FluxQuiverItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register(ID_FLUX_TRIDENT, () -> new FluxTridentItem(MATERIAL_FLUX_METAL, 2, 5, -2.9F, 2.0F, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair().setISTER(() -> FluxTridentISTER::new), energy, xfer));

        ITEMS.register("flux_shovel", () -> new FluxShovelItem(MATERIAL_FLUX_METAL, 1.5F, -3.0F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_pickaxe", () -> new FluxPickaxeItem(MATERIAL_FLUX_METAL, 1, -2.8F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_axe", () -> new FluxAxeItem(MATERIAL_FLUX_METAL, 6.0F, -3.0F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_hoe", () -> new FluxHoeItem(MATERIAL_FLUX_METAL, -1.0F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_excavator", () -> new FluxExcavatorItem(MATERIAL_FLUX_METAL, 2.0F, -3.0F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_hammer", () -> new FluxHammerItem(MATERIAL_FLUX_METAL, 6.0F, -3.2F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_fishing_rod", () -> new FluxFishingRodItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), 0, 0, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register(ID_FLUX_WRENCH, () -> new FluxWrenchItem(MATERIAL_FLUX_METAL, 2, -2.0F, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));

        ITEMS.register("flux_elytra", () -> new FluxElytraItem(FLUX_ELYTRA, EquipmentSlotType.CHEST, new Item.Properties().stacksTo(1).tab(transportation).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_controller", () -> new FluxElytraControllerItem(new Item.Properties().stacksTo(1).tab(transportation).rarity(rarity).setNoRepair()));
    }

    public static final IItemTier MATERIAL_FLUX_METAL = new ItemTierCoFH(4, 0, 8.0F, 3.0F, 18, () -> Ingredient.EMPTY);
    public static final ArmorMaterialCoFH FLUX_ELYTRA = new ArmorMaterialCoFH("redstone_arsenal:elytra", 0, new int[]{0, 0, 5, 0}, 18, SoundEvents.ARMOR_EQUIP_ELYTRA, 1.0F, 0.0F, () -> MATERIAL_FLUX_METAL.getRepairIngredient());

}
