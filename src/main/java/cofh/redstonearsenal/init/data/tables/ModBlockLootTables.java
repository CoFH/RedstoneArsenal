package cofh.redstonearsenal.init.data.tables;

import cofh.lib.init.data.loot.BlockLootSubProviderCoFH;

import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
import static cofh.redstonearsenal.init.registries.ModIDs.ID_FLUX_GEM_BLOCK;
import static cofh.redstonearsenal.init.registries.ModIDs.ID_FLUX_METAL_BLOCK;

public class ModBlockLootTables extends BlockLootSubProviderCoFH {

    @Override
    protected void generate() {

        createSimpleDropTable(BLOCKS.get(ID_FLUX_METAL_BLOCK));
        createSimpleDropTable(BLOCKS.get(ID_FLUX_GEM_BLOCK));
    }

}
