package cofh.redstonearsenal.init;

import cofh.core.item.BlockItemCoFH;
import cofh.core.item.ItemCoFH;
import cofh.lib.item.ArmorMaterialCoFH;
import cofh.lib.item.ItemTierCoFH;
import cofh.redstonearsenal.item.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import static cofh.redstonearsenal.RedstoneArsenal.*;
import static cofh.redstonearsenal.init.RSAIDs.*;

public class RSAItems {

    private RSAItems() {

    }

    public static void register() {

        CreativeModeTab combat = RSA_GROUP;
        CreativeModeTab transportation = RSA_GROUP;
        CreativeModeTab tools = RSA_GROUP;
        CreativeModeTab misc = RSA_GROUP;

        Rarity rarity = Rarity.UNCOMMON;

        int energy = 800000;
        int xfer = 10000;

        ITEMS.register(ID_FLUX_GEM, () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register(ID_FLUX_INGOT, () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register(ID_FLUX_NUGGET, () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register(ID_FLUX_DUST, () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register(ID_FLUX_GEAR, () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));

        ITEMS.register("flux_plating", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register("obsidian_rod", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register("flux_obsidian_rod", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));

        ITEMS.register(ID_FLUX_METAL_BLOCK, () -> new BlockItemCoFH(BLOCKS.get(ID_FLUX_METAL_BLOCK), new Item.Properties().tab(misc).rarity(rarity)));
        ITEMS.register(ID_FLUX_GEM_BLOCK, () -> new BlockItemCoFH(BLOCKS.get(ID_FLUX_GEM_BLOCK), new Item.Properties().tab(misc).rarity(rarity)));

        //ITEMS.register("flux_plate", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));
        //ITEMS.register("flux_coin", () -> new ItemCoFH(new Item.Properties().tab(misc).rarity(rarity)));

        ITEMS.register(ID_FLUX_SWORD, () -> new FluxSwordItem(MATERIAL_FLUX_METAL, 4, -2.4F, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_shield", () -> new FluxShieldItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_bow", () -> new FluxBowItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), 1, 1, 1, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), Mth.floor(energy * 0.4F), xfer));
        ITEMS.register("flux_crossbow", () -> new FluxCrossbowItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), 1, 1, 1, 3, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), Mth.floor(energy * 0.4F), xfer));
        ITEMS.register("flux_quiver", () -> new FluxQuiverItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), Mth.floor(energy * 0.4F), xfer, 2.0F));
        ITEMS.register(ID_FLUX_TRIDENT, () -> new FluxTridentItem(MATERIAL_FLUX_METAL, 2, 7, -2.9F, 2.0F, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));

        ITEMS.register("flux_shovel", () -> new FluxShovelItem(MATERIAL_FLUX_METAL, 2.5F, -3.0F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_pickaxe", () -> new FluxPickaxeItem(MATERIAL_FLUX_METAL, 2, -2.8F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_axe", () -> new FluxAxeItem(MATERIAL_FLUX_METAL, 6.5F, -3.0F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        //ITEMS.register("flux_hoe", () -> new FluxHoeItem(MATERIAL_FLUX_METAL, -1.0F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_sickle", () -> new FluxSickleItem(MATERIAL_FLUX_METAL, 3.5F, -2.6F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_excavator", () -> new FluxExcavatorItem(MATERIAL_FLUX_METAL, 3.0F, -3.0F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_hammer", () -> new FluxHammerItem(MATERIAL_FLUX_METAL, 6.0F, -3.4F, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_fishing_rod", () -> new FluxFishingRodItem(MATERIAL_FLUX_METAL.getEnchantmentValue(), 0, 0, new Item.Properties().stacksTo(1).tab(tools).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register(ID_FLUX_WRENCH, () -> new FluxWrenchItem(MATERIAL_FLUX_METAL, 1.5F, -2.0F, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));

        ITEMS.register("flux_helmet", () -> new FluxArmorItem(FLUX_ARMOR, EquipmentSlot.HEAD, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_chestplate", () -> new FluxArmorItem(FLUX_ARMOR, EquipmentSlot.CHEST, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_leggings", () -> new FluxArmorItem(FLUX_ARMOR, EquipmentSlot.LEGS, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_boots", () -> new FluxArmorItem(FLUX_ARMOR, EquipmentSlot.FEET, new Item.Properties().stacksTo(1).tab(combat).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_elytra", () -> new FluxElytraItem(FLUX_ELYTRA, EquipmentSlot.CHEST, new Item.Properties().stacksTo(1).tab(transportation).rarity(rarity).setNoRepair(), energy, xfer));
        ITEMS.register("flux_controller", () -> new FluxElytraControllerItem(new Item.Properties().stacksTo(1).tab(transportation).rarity(rarity).setNoRepair()));
    }

    public static void setup() {

        FluxWrenchItem.initEnchants();
        FluxSickleItem.initEnchants();
    }

    public static final Tier MATERIAL_FLUX_METAL = new ItemTierCoFH(4, 0, 8.0F, 2.5F, 18, () -> Ingredient.EMPTY);
    public static final ArmorMaterialCoFH FLUX_ELYTRA = new ArmorMaterialCoFH("redstone_arsenal:elytra", 0, new int[]{0, 0, 5, 0}, 18, SoundEvents.ARMOR_EQUIP_ELYTRA, 1.0F, 0.0F, MATERIAL_FLUX_METAL::getRepairIngredient);
    public static final ArmorMaterialCoFH FLUX_ARMOR = new ArmorMaterialCoFH("redstone_arsenal:armor", 0, new int[]{3, 6, 8, 3}, 18, SoundEvents.ARMOR_EQUIP_GOLD, 1.0F, 0.0F, MATERIAL_FLUX_METAL::getRepairIngredient);

}
