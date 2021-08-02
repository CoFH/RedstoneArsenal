package cofh.redstonearsenal;

import cofh.lib.util.DeferredRegisterCoFH;
import cofh.redstonearsenal.init.RSABlocks;
import cofh.redstonearsenal.init.RSAConfig;
import cofh.redstonearsenal.init.RSAItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

@Mod(ID_REDSTONE_ARSENAL)
public class RedstoneArsenal {

    public static final Logger LOG = LogManager.getLogger(ID_REDSTONE_ARSENAL);

    public static final DeferredRegisterCoFH<Block> BLOCKS = DeferredRegisterCoFH.create(ForgeRegistries.BLOCKS, ID_REDSTONE_ARSENAL);
    public static final DeferredRegisterCoFH<Item> ITEMS = DeferredRegisterCoFH.create(ForgeRegistries.ITEMS, ID_REDSTONE_ARSENAL);

    public static ItemGroup itemGroup;

    public RedstoneArsenal() {

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        RSABlocks.register();
        RSAItems.register();
    }

    // region INITIALIZATION
    private void commonSetup(final FMLCommonSetupEvent event) {

        RSAConfig.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {

        if (RSAConfig.enableCreativeTab.get()) {
            itemGroup = new ItemGroup(-1, ID_REDSTONE_ARSENAL) {

                @Override
                @OnlyIn(Dist.CLIENT)
                public ItemStack createIcon() {

                    return new ItemStack(ITEMS.get("flux_sword"));
                }
            };
        }
    }
    // endregion
}
