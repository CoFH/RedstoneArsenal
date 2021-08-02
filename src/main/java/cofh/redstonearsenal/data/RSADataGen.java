package cofh.redstonearsenal.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ID_REDSTONE_ARSENAL)
public class RSADataGen {

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {

        if (event.includeServer()) {
            registerServerProviders(event);
        }
        if (event.includeClient()) {
            registerClientProviders(event);
        }
    }

    private static void registerServerProviders(GatherDataEvent event) {

        DataGenerator gen = event.getGenerator();
        ExistingFileHelper exFileHelper = event.getExistingFileHelper();

        gen.addProvider(new RSALootTableProvider(gen));
        gen.addProvider(new RSARecipeProvider(gen));
    }

    private static void registerClientProviders(GatherDataEvent event) {

        DataGenerator gen = event.getGenerator();
        ExistingFileHelper exFileHelper = event.getExistingFileHelper();

        gen.addProvider(new RSABlockStates(gen, exFileHelper));
        gen.addProvider(new RSAItemModels(gen, exFileHelper));
    }

}
