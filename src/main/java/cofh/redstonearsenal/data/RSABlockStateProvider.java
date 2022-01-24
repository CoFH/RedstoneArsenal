package cofh.redstonearsenal.data;

import cofh.lib.data.BlockStateProviderCoFH;
import cofh.lib.util.DeferredRegisterCoFH;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_GEM_BLOCK;
import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_METAL_BLOCK;

public class RSABlockStateProvider extends BlockStateProviderCoFH {

    public RSABlockStateProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {

        super(gen, ID_REDSTONE_ARSENAL, existingFileHelper);
    }

    @Override
    public String getName() {

        return "Redstone Arsenal: BlockStates";
    }

    @Override
    protected void registerStatesAndModels() {

        DeferredRegisterCoFH<Block> reg = BLOCKS;

        simpleBlock(reg.getSup(ID_FLUX_METAL_BLOCK));
        simpleBlock(reg.getSup(ID_FLUX_GEM_BLOCK));
    }

}
