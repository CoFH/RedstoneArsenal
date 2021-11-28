package cofh.redstonearsenal.data;

import cofh.lib.data.ItemModelProviderCoFH;
import cofh.lib.util.DeferredRegisterCoFH;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;

public class RSAItemModelProvider extends ItemModelProviderCoFH {

    public RSAItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {

        super(generator, ID_REDSTONE_ARSENAL, existingFileHelper);
    }

    @Override
    public String getName() {

        return "Redstone Arsenal: Item Models";
    }

    @Override
    protected void registerModels() {

        registerBlockItemModels();

        DeferredRegisterCoFH<Item> reg = ITEMS;

        generated(reg.getSup("flux_gem"));
        generated(reg.getSup("flux_ingot"));
        generated(reg.getSup("flux_nugget"));
        generated(reg.getSup("flux_dust"));
        generated(reg.getSup("flux_gear"));
        // generated(reg.getSup("flux_plate"));
        // generated(reg.getSup("flux_coin"));
    }

    // region HELPERS
    private void registerBlockItemModels() {

        DeferredRegisterCoFH<Block> reg = BLOCKS;

    }
    // endregion
}
