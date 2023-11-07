package cofh.redstonearsenal.util;

import cofh.core.common.capability.CapabilityRedstoneFlux;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * This class contains helper functions related to Redstone Flux, aka the Forge Energy system.
 *
 * @author King Lemming
 */
public class RSAEnergyHelper {

    public static boolean standaloneRedstoneFlux;

    private RSAEnergyHelper() {

    }

    public static boolean hasEnergyHandlerCap(ItemStack item) {

        return !item.isEmpty() && item.getCapability(getBaseEnergySystem()).isPresent();
    }

    public static Capability<? extends IEnergyStorage> getBaseEnergySystem() {

        return standaloneRedstoneFlux ? CapabilityRedstoneFlux.RF_ENERGY : ForgeCapabilities.ENERGY;
    }

}
