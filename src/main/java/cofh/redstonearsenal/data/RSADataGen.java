//package cofh.redstonearsenal.data;
//
//import net.minecraft.data.DataGenerator;
//import net.minecraftforge.common.data.ExistingFileHelper;
//import net.minecraftforge.data.event.GatherDataEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
//import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;
//
//@Mod.EventBusSubscriber (bus = Mod.EventBusSubscriber.Bus.MOD, modid = ID_REDSTONE_ARSENAL)
//public class RSADataGen {
//
//    @SubscribeEvent
//    public static void gatherData(final GatherDataEvent event) {
//
//        DataGenerator gen = event.getGenerator();
//        ExistingFileHelper exFileHelper = event.getExistingFileHelper();
//
//        gen.addProvider(event.includeServer(), new RSALootTableProvider(gen));
//        gen.addProvider(event.includeServer(), new RSARecipeProvider(gen));
//
//        gen.addProvider(event.includeClient(), new RSABlockStateProvider(gen, exFileHelper));
//        gen.addProvider(event.includeClient(), new RSAItemModelProvider(gen, exFileHelper));
//    }
//
//}
