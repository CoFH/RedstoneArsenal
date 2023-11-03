package cofh.redstonearsenal.data.providers;

import cofh.lib.data.LootTableProviderCoFH;
import cofh.redstonearsenal.data.tables.ModBlockLootTables;
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