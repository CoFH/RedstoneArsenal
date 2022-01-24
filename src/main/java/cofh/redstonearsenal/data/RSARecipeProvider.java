package cofh.redstonearsenal.data;

import cofh.lib.data.RecipeProviderCoFH;
import cofh.lib.util.DeferredRegisterCoFH;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;

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
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {

        DeferredRegisterCoFH<Item> reg = ITEMS;

        generateStorageRecipes(consumer, reg.get(ID_FLUX_METAL_BLOCK), reg.get(ID_FLUX_INGOT));
        generateStorageRecipes(consumer, reg.get(ID_FLUX_GEM_BLOCK), reg.get(ID_FLUX_GEM));
        generateStorageRecipes(consumer, reg.get(ID_FLUX_INGOT), reg.get(ID_FLUX_NUGGET), "_from_nuggets", "_from_ingot");

        generateSmeltingAndBlastingRecipes(reg, consumer, "flux", 0);
    }

}
