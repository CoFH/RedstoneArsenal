package cofh.redstonearsenal.common.config;

import cofh.core.common.config.IBaseConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Supplier;

import static cofh.lib.util.Constants.FALSE;

public class RSAConfig implements IBaseConfig {

    @Override
    public void apply(ModConfigSpec.Builder builder) {

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
