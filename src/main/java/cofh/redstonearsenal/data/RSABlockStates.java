package cofh.redstonearsenal.data;

import cofh.lib.data.BlockStateProviderCoFH;
import cofh.lib.util.DeferredRegisterCoFH;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;

public class RSABlockStates extends BlockStateProviderCoFH {

    public RSABlockStates(DataGenerator gen, ExistingFileHelper existingFileHelper) {

        super(gen, ID_REDSTONE_ARSENAL, existingFileHelper);
    }

    @Override
    public String getName() {

        return "Redstone Arsenal: BlockStates";
    }

    @Override
    protected void registerStatesAndModels() {

        DeferredRegisterCoFH<Block> reg = BLOCKS;

    }

}
