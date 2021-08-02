package cofh.redstonearsenal.data;

import cofh.lib.data.RecipeProviderCoFH;
import cofh.lib.util.DeferredRegisterCoFH;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;

import java.util.function.Consumer;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;

public class RSARecipeProvider extends RecipeProviderCoFH {

    public RSARecipeProvider(DataGenerator generatorIn) {

        super(generatorIn, ID_REDSTONE_ARSENAL);
    }

    @Override
    public String getName() {

        return "Redstone Arsenal: Recipes";
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {

        DeferredRegisterCoFH<Item> reg = ITEMS;

    }

}
