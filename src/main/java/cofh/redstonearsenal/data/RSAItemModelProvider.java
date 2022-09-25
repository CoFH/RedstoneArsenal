package cofh.redstonearsenal.data;

import cofh.lib.data.ItemModelProviderCoFH;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;
import static cofh.redstonearsenal.init.RSAIDs.*;

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

        var reg = ITEMS;

        generated(reg.getSup(ID_FLUX_GEM), MATERIALS);
        generated(reg.getSup(ID_FLUX_INGOT), MATERIALS);
        generated(reg.getSup(ID_FLUX_NUGGET), MATERIALS);
        generated(reg.getSup(ID_FLUX_DUST), MATERIALS);
        generated(reg.getSup(ID_FLUX_GEAR), MATERIALS);

        // generated(reg.getSup("flux_plate"));
        // generated(reg.getSup("flux_coin"));
    }

    // region HELPERS
    private void registerBlockItemModels() {

        var reg = BLOCKS;

        blockItem(reg.getSup(ID_FLUX_METAL_BLOCK));
        blockItem(reg.getSup(ID_FLUX_GEM_BLOCK));
    }
    // endregion
}
