package cofh.redstonearsenal.data;

import cofh.redstonearsenal.data.providers.ModBlockStateProvider;
import cofh.redstonearsenal.data.providers.ModItemModelProvider;
import cofh.redstonearsenal.data.providers.ModLootTableProvider;
import cofh.redstonearsenal.data.providers.ModRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

@Mod.EventBusSubscriber (bus = Mod.EventBusSubscriber.Bus.MOD, modid = ID_REDSTONE_ARSENAL)
public class ModDataGen {

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {

        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper exFileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new ModLootTableProvider(output));
        gen.addProvider(event.includeServer(), new ModRecipeProvider(output));

        gen.addProvider(event.includeClient(), new ModBlockStateProvider(output, exFileHelper));
        gen.addProvider(event.includeClient(), new ModItemModelProvider(output, exFileHelper));
    }

}
