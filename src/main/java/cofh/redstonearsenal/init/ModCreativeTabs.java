package cofh.redstonearsenal.init;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.CREATIVE_TABS;
import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;
import static cofh.redstonearsenal.init.ModIDs.ID_FLUX_SWORD;

public class ModCreativeTabs {

    private ModCreativeTabs() {

    }

    public static void register() {

    }

    private static final RegistryObject<CreativeModeTab> TAB = CREATIVE_TABS.register(ID_REDSTONE_ARSENAL, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.redstone_arsenal"))
            .icon(() -> new ItemStack(ITEMS.get(ID_FLUX_SWORD)))
            .displayItems((parameters, output) -> ModItems.CREATIVE_TAB_ITEMS.forEach((item) -> output.accept(item.get())))
            .build());

}