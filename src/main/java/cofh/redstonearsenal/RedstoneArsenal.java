package cofh.redstonearsenal;

import cofh.core.config.ConfigManager;
import cofh.core.event.CoreClientEvents;
import cofh.lib.network.PacketHandler;
import cofh.lib.util.DeferredRegisterCoFH;
import cofh.redstonearsenal.capability.CapabilityFluxShielding;
import cofh.redstonearsenal.config.RSAConfig;
import cofh.redstonearsenal.init.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

@Mod (ID_REDSTONE_ARSENAL)
public class RedstoneArsenal {

    public static final Logger LOG = LogManager.getLogger(ID_REDSTONE_ARSENAL);
    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();

    public static final PacketHandler PACKET_HANDLER = new PacketHandler(new ResourceLocation(ID_REDSTONE_ARSENAL, "flux_shielding"), LOG);

    public static final DeferredRegisterCoFH<Block> BLOCKS = DeferredRegisterCoFH.create(ForgeRegistries.BLOCKS, ID_REDSTONE_ARSENAL);
    public static final DeferredRegisterCoFH<Item> ITEMS = DeferredRegisterCoFH.create(ForgeRegistries.ITEMS, ID_REDSTONE_ARSENAL);
    public static final DeferredRegisterCoFH<CreativeModeTab> CREATIVE_TABS = DeferredRegisterCoFH.create(Registries.CREATIVE_MODE_TAB, ID_REDSTONE_ARSENAL);

    public static final DeferredRegisterCoFH<EntityType<?>> ENTITIES = DeferredRegisterCoFH.create(ForgeRegistries.ENTITY_TYPES, ID_REDSTONE_ARSENAL);
    public static final DeferredRegisterCoFH<SoundEvent> SOUND_EVENTS = DeferredRegisterCoFH.create(ForgeRegistries.SOUND_EVENTS, ID_REDSTONE_ARSENAL);

    public RedstoneArsenal() {

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CONFIG_MANAGER.register(modEventBus)
                .addServerConfig(new RSAConfig());
        CONFIG_MANAGER.setupServer();

        modEventBus.addListener(this::capSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        // modEventBus.addListener(this::creativeTabSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        ENTITIES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);

        ModBlocks.register();
        ModItems.register();
        ModCreativeTabs.register();

        ModEntities.register();
        ModPackets.register();
        ModSounds.register();
    }

    // region INITIALIZATION
    private void capSetup(RegisterCapabilitiesEvent event) {

        CapabilityFluxShielding.register(event);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        event.enqueueWork(ModItems::setup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {

        event.enqueueWork(() -> CoreClientEvents.addNamespace(ID_REDSTONE_ARSENAL));
    }

    private void creativeTabSetup(final BuildCreativeModeTabContentsEvent event) {

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {

        }
    }
    // endregion
}
