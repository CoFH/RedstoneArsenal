package cofh.redstonearsenal.init;

import cofh.redstonearsenal.util.RSAEnergyHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class RSAConfig {

    private static boolean registered = false;

    public static void register() {

        if (registered) {
            return;
        }
        FMLJavaModLoadingContext.get().getModEventBus().register(RSAConfig.class);
        registered = true;

        genServerConfig();
        // genClientConfig();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec);
    }

    private RSAConfig() {

    }

    // region CONFIG SPEC
    private static final ForgeConfigSpec.Builder SERVER_CONFIG = new ForgeConfigSpec.Builder();
    private static ForgeConfigSpec serverSpec;

    private static final ForgeConfigSpec.Builder CLIENT_CONFIG = new ForgeConfigSpec.Builder();
    private static ForgeConfigSpec clientSpec;

    private static void genServerConfig() {

        standaloneRedstoneFlux = SERVER_CONFIG
                .comment("If TRUE, Redstone Flux will act as its own energy system and will NOT be interoperable with 'Forge Energy' - only enable this if you absolutely know what you are doing and want Redstone Arsenal to use a unique energy system.")
                .define("Standalone Redstone Flux", false);

        serverSpec = SERVER_CONFIG.build();

        refreshServerConfig();
    }

    //    private static void genClientConfig() {
    //
    //        enableCreativeTab = CLIENT_CONFIG
    //                .comment("If TRUE, Redstone Arsenal will have its own Item Group (Creative Tab).")
    //                .define("Enable Item Group", true);
    //
    //        clientSpec = CLIENT_CONFIG.build();
    //
    //        refreshClientConfig();
    //    }

    private static void refreshServerConfig() {

        RSAEnergyHelper.standaloneRedstoneFlux = standaloneRedstoneFlux.get();
    }

    private static void refreshClientConfig() {

    }
    // endregion

    // region SERVER VARIABLES
    public static BooleanValue standaloneRedstoneFlux;
    // endregion

    // region CLIENT VARIABLES
    //public static BooleanValue enableCreativeTab;
    // endregion

    // region CONFIGURATION
    @SubscribeEvent
    public static void configLoading(ModConfig.Loading event) {

        switch (event.getConfig().getType()) {
            case CLIENT:
                refreshClientConfig();
                break;
            case SERVER:
                refreshServerConfig();
        }
    }

    @SubscribeEvent
    public static void configReloading(ModConfig.Reloading event) {

        switch (event.getConfig().getType()) {
            case CLIENT:
                refreshClientConfig();
                break;
            case SERVER:
                refreshServerConfig();
        }
    }
    // endregion
}
