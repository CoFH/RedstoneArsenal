package cofh.redstonearsenal.data;

import cofh.lib.data.LootTableProviderCoFH;
import cofh.lib.util.DeferredRegisterCoFH;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;

import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;
import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_GEM_BLOCK;
import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_METAL_BLOCK;

public class RSALootTableProvider extends LootTableProviderCoFH {

    public RSALootTableProvider(DataGenerator gen) {

        super(gen);
    }

    @Override
    public String getName() {

        return "Redstone Arsenal: Loot Tables";
    }

    @Override
    protected void addTables() {

        DeferredRegisterCoFH<Block> regBlocks = BLOCKS;
        DeferredRegisterCoFH<Item> regItems = ITEMS;

        createSimpleDropTable(regBlocks.get(ID_FLUX_METAL_BLOCK));
        createSimpleDropTable(regBlocks.get(ID_FLUX_GEM_BLOCK));
    }

}