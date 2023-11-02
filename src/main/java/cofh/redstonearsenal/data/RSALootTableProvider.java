//package cofh.redstonearsenal.data;
//
//import cofh.lib.data.LootTableProviderCoFH;
//import net.minecraft.data.DataGenerator;
//
//import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
//import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;
//import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_GEM_BLOCK;
//import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_METAL_BLOCK;
//
//public class RSALootTableProvider extends LootTableProviderCoFH {
//
//    public RSALootTableProvider(DataGenerator gen) {
//
//        super(gen);
//    }
//
//    @Override
//    public String getName() {
//
//        return "Redstone Arsenal: Loot Tables";
//    }
//
//    @Override
//    protected void addTables() {
//
//        var regBlocks = BLOCKS;
//        var regItems = ITEMS;
//
//        createSimpleDropTable(regBlocks.get(ID_FLUX_METAL_BLOCK));
//        createSimpleDropTable(regBlocks.get(ID_FLUX_GEM_BLOCK));
//    }
//
//}