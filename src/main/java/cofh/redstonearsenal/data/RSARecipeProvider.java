package cofh.redstonearsenal.data;

import cofh.lib.data.RecipeProviderCoFH;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;
import static cofh.redstonearsenal.init.RSAIDs.*;

public class RSARecipeProvider extends RecipeProviderCoFH {

    public RSARecipeProvider(DataGenerator generatorIn) {

        super(generatorIn, ID_REDSTONE_ARSENAL);
    }

    @Override
    public String getName() {

        return "Redstone Arsenal: Recipes";
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {

        var reg = ITEMS;

        generateStorageRecipes(consumer, reg.get(ID_FLUX_METAL_BLOCK), reg.get(ID_FLUX_INGOT));
        generateStorageRecipes(consumer, reg.get(ID_FLUX_GEM_BLOCK), reg.get(ID_FLUX_GEM));
        generateStorageRecipes(consumer, reg.get(ID_FLUX_INGOT), reg.get(ID_FLUX_NUGGET), "_from_nuggets", "_from_ingot");

        generateSmeltingAndBlastingRecipes(reg, consumer, "flux", 0);
    }

}
