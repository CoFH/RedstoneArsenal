package cofh.redstonearsenal.init.data.providers;

import cofh.lib.init.data.LootTableProviderCoFH;
import cofh.redstonearsenal.init.data.tables.ModBlockLootTables;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

public class ModLootTableProvider extends LootTableProviderCoFH {

    public ModLootTableProvider(PackOutput output) {

        super(output, List.of(
                new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)
        ));
    }

}