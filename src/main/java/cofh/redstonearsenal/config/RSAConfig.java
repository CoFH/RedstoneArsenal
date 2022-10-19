package cofh.redstonearsenal.config;

import cofh.core.config.IBaseConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Supplier;

import static cofh.lib.util.Constants.FALSE;

public class RSAConfig implements IBaseConfig {

    @Override
    public void apply(ForgeConfigSpec.Builder builder) {

        builder.push("Global Options");

        boolStandaloneRedstoneFlux = builder
                .comment("If TRUE, Redstone Flux will act as its own energy system and will NOT be interoperable with 'Forge Energy' - only enable this if you absolutely know what you are doing and want the Thermal Series to use a unique energy system.")
                .define("Standalone Redstone Flux", boolStandaloneRedstoneFlux);

        builder.pop();
    }

    // region CONFIG VARIABLES
    private Supplier<Boolean> boolStandaloneRedstoneFlux = FALSE;
    // endregion
}
