package cofh.redstonearsenal.data.providers;

import cofh.lib.data.BlockStateProviderCoFH;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
import static cofh.redstonearsenal.init.ModIDs.ID_FLUX_GEM_BLOCK;
import static cofh.redstonearsenal.init.ModIDs.ID_FLUX_METAL_BLOCK;

public class ModBlockStateProvider extends BlockStateProviderCoFH {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {

        super(output, ID_REDSTONE_ARSENAL, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

        var reg = BLOCKS;

        simpleBlock(reg.getSup(ID_FLUX_METAL_BLOCK));
        simpleBlock(reg.getSup(ID_FLUX_GEM_BLOCK));
    }

}
