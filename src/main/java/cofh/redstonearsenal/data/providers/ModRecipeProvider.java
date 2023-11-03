package cofh.redstonearsenal.data.providers;

import cofh.lib.data.RecipeProviderCoFH;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;
import static cofh.redstonearsenal.init.ModIDs.*;

public class ModRecipeProvider extends RecipeProviderCoFH {

    public ModRecipeProvider(PackOutput output) {

        super(output, ID_REDSTONE_ARSENAL);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        var reg = ITEMS;

        generateStorageRecipes(consumer, reg.get(ID_FLUX_METAL_BLOCK), reg.get(ID_FLUX_INGOT));
        generateStorageRecipes(consumer, reg.get(ID_FLUX_GEM_BLOCK), reg.get(ID_FLUX_GEM));
        generateStorageRecipes(consumer, reg.get(ID_FLUX_INGOT), reg.get(ID_FLUX_NUGGET), "_from_nuggets", "_from_ingot");

        generateSmeltingAndBlastingRecipes(reg, consumer, "flux", 0);
    }

}
