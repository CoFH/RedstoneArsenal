package cofh.redstonearsenal.init;

import cofh.core.config.IBaseConfig;
import cofh.redstonearsenal.util.RSAEnergyHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class RSAServerConfig implements IBaseConfig {

    private static boolean registered = false;

    @Override
    public void apply(ForgeConfigSpec.Builder builder) {

        boolStandaloneRedstoneFlux = builder
                .comment("If TRUE, Redstone Flux will act as its own energy system and will NOT be interoperable with 'Forge Energy' - only enable this if you absolutely know what you are doing and want Redstone Arsenal to use a unique energy system.")
                .define("Standalone Redstone Flux", false);
    }

    @Override
    public void refresh() {

        RSAEnergyHelper.standaloneRedstoneFlux = boolStandaloneRedstoneFlux.get();
    }

    // region CONFIG VARIABLES
    public static BooleanValue boolStandaloneRedstoneFlux;
    // endregion

}
