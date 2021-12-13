package cofh.redstonearsenal.capability;

import cofh.redstonearsenal.item.FluxArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

import static cofh.redstonearsenal.RedstoneArsenal.ITEMS;

public class CapabilityFluxShielding {

    private static boolean registered = false;

    @CapabilityInject (IFluxShieldedItem.class)
    public static Capability<IFluxShieldedItem> FLUX_SHIELDED_ITEM_CAPABILITY = null;

    public static void register() {

        if (registered) {
            return;
        }
        registered = true;

        CapabilityManager.INSTANCE.register(IFluxShieldedItem.class, new DefaultFluxShieldHandlerStorage<>(), () -> new FluxShieldedEnergyItemWrapper(new ItemStack(ITEMS.get("flux_chestplate")), FluxArmorItem.ENERGY_PER_USE_EMPOWERED));
    }

    private static class DefaultFluxShieldHandlerStorage<T extends IFluxShieldedItem> implements Capability.IStorage<T> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<T> capability, T instance, Direction side) {

            return null;
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {

        }

    }

}
