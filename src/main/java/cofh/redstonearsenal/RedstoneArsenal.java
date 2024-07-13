package cofh.redstonearsenal;

import cofh.core.client.event.CoreClientEvents;
import cofh.core.common.capability.CoreCapabilities;
import cofh.core.common.config.ConfigManager;
import cofh.lib.common.energy.EnergyContainerItemWrapper;
import cofh.lib.util.DeferredRegisterCoFH;
import cofh.redstonearsenal.common.capability.CapabilityFluxShielding;
import cofh.redstonearsenal.common.capability.FluxShieldedEnergyItemWrapper;
import cofh.redstonearsenal.common.config.RSAConfig;
import cofh.redstonearsenal.common.item.*;
import cofh.redstonearsenal.common.network.PacketHandler;
import cofh.redstonearsenal.init.registries.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

@Mod (ID_REDSTONE_ARSENAL)
public class RedstoneArsenal {

    public static final Logger LOG = LogManager.getLogger(ID_REDSTONE_ARSENAL);
    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();

    // public static final PacketHandler PACKET_HANDLER = new PacketHandler(new ResourceLocation(ID_REDSTONE_ARSENAL, "flux_shielding"), LOG);

    public static final DeferredRegisterCoFH<Block> BLOCKS = DeferredRegisterCoFH.create(BuiltInRegistries.BLOCK, ID_REDSTONE_ARSENAL);
    public static final DeferredRegisterCoFH<Item> ITEMS = DeferredRegisterCoFH.create(BuiltInRegistries.ITEM, ID_REDSTONE_ARSENAL);
    public static final DeferredRegisterCoFH<CreativeModeTab> CREATIVE_TABS = DeferredRegisterCoFH.create(BuiltInRegistries.CREATIVE_MODE_TAB, ID_REDSTONE_ARSENAL);

    public static final DeferredRegisterCoFH<EntityType<?>> ENTITIES = DeferredRegisterCoFH.create(BuiltInRegistries.ENTITY_TYPE, ID_REDSTONE_ARSENAL);
    public static final DeferredRegisterCoFH<SoundEvent> SOUND_EVENTS = DeferredRegisterCoFH.create(BuiltInRegistries.SOUND_EVENT, ID_REDSTONE_ARSENAL);

    public RedstoneArsenal(ModContainer modContainer, IEventBus modEventBus) {

        CONFIG_MANAGER.register(modEventBus)
                .addServerConfig(new RSAConfig());
        CONFIG_MANAGER.setupServer();

        modEventBus.addListener(this::capSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        // modEventBus.addListener(this::creativeTabSetup);

        modEventBus.addListener(PacketHandler::registerNetworking);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        ENTITIES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);

        ModBlocks.register();
        ModItems.register();
        ModCreativeTabs.register();

        ModEntities.register();
        ModSounds.register();
    }

    // region INITIALIZATION
    private void capSetup(RegisterCapabilitiesEvent event) {

        ITEMS.getRegistryObjects().values().forEach((holder) -> {

            if (holder.value() instanceof IFluxItem fluxItem) {
                event.registerItem(Capabilities.EnergyStorage.ITEM, (itemStack, context) -> new EnergyContainerItemWrapper(itemStack, fluxItem), holder.value());
            }
            if (holder.value() instanceof FluxArmorItem fluxArmor) {
                event.registerItem(CapabilityFluxShielding.ITEM, (itemStack, context) -> new FluxShieldedEnergyItemWrapper(itemStack, fluxArmor.getEnergyPerUse(true)), holder.value());
            }

            if (holder.value() instanceof FluxAxeItem axe) {
                event.registerItem(CoreCapabilities.AreaEffectHandler.ITEM, (itemStack, context) -> new FluxAxeItem.AreaWrapper(itemStack, axe), holder.value());
            }
            if (holder.value() instanceof FluxExcavatorItem excavator) {
                event.registerItem(CoreCapabilities.AreaEffectHandler.ITEM, (itemStack, context) -> new FluxExcavatorItem.AreaWrapper(itemStack, excavator), holder.value());
            }
            if (holder.value() instanceof FluxHammerItem hammer) {
                event.registerItem(CoreCapabilities.AreaEffectHandler.ITEM, (itemStack, context) -> new FluxHammerItem.AreaWrapper(itemStack, hammer), holder.value());
            }
            if (holder.value() instanceof FluxSickleItem sickle) {
                event.registerItem(CoreCapabilities.AreaEffectHandler.ITEM, (itemStack, context) -> new FluxSickleItem.AreaWrapper(itemStack, sickle), holder.value());
            }
            if (holder.value() instanceof FluxShieldItem shield) {
                event.registerItem(CoreCapabilities.ShieldHandler.ITEM, (itemStack, context) -> new FluxShieldItem.ShieldWrapper(itemStack, shield), holder.value());
            }
            if (holder.value() instanceof FluxBowItem bow) {
                event.registerItem(CoreCapabilities.ArcheryHandler.BOW, (itemStack, context) -> new FluxBowItem.BowWrapper(itemStack, bow), holder.value());
            }
            if (holder.value() instanceof FluxQuiverItem quiver) {
                event.registerItem(CoreCapabilities.ArcheryHandler.AMMO, (itemStack, context) -> new FluxQuiverItem.AmmoWrapper(itemStack, quiver), holder.value());
            }
        });
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

    public static final ResourceKey<DamageType> FLUX = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ID_REDSTONE_ARSENAL, "flux"));
    public static final ResourceKey<DamageType> FLUX_RANGED = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ID_REDSTONE_ARSENAL, "flux_ranged"));

}
