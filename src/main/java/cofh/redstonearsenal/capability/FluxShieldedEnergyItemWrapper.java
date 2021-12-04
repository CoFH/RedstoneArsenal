package cofh.redstonearsenal.capability;

import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.util.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static cofh.redstonearsenal.capability.CapabilityFluxShielding.FLUX_SHIELDED_ITEM_CAPABILITY;

/**
 * Standard implementation for the IFluxShieldItem capability.
 * An energy container with 1 shield charge that takes 30 seconds to regenerate.
 *
 * @author Hekera
 * why am i here
 */
public class FluxShieldedEnergyItemWrapper extends EnergyContainerItemWrapper implements IFluxShieldedItem {

    private final LazyOptional<IFluxShieldedItem> holder = LazyOptional.of(() -> this);

    protected final ItemStack shieldedItem;
    protected final int COOLDOWN = 600;
    protected int energyPerUse;

    public FluxShieldedEnergyItemWrapper(ItemStack shieldedItemContainer, int energyPerUse) {

        super(shieldedItemContainer, (IEnergyContainerItem) shieldedItemContainer.getItem());
        this.shieldedItem = shieldedItemContainer;
        this.energyPerUse = energyPerUse;
    }

    @Override
    public int availableCharges(LivingEntity entity) {

        CompoundNBT nbt = shieldedItem.getOrCreateTag();
        if (getEnergyStored() < energyPerUse) {
            return 0;
        }
        if (!nbt.contains("fluxShield")) {
            return 1;
        }
        return entity.level.getGameTime() >= nbt.getLong("fluxShield") ? 1 : 0;
    }

    @Override
    public int maxCharges(LivingEntity entity) {

        return 1;
    }

    @Override
    public boolean useCharge(LivingEntity entity) {

        if (availableCharges(entity) < 1 || extractEnergy(energyPerUse, Utils.isCreativePlayer(entity)) != energyPerUse) {
            return false;
        }
        long regenTime = entity.level.getGameTime() + COOLDOWN;
        shieldedItem.getOrCreateTag().putLong("fluxShield", regenTime);
        return true;
    }

    // region ICapabilityProvider
    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

        if (cap == FLUX_SHIELDED_ITEM_CAPABILITY) {
            return FLUX_SHIELDED_ITEM_CAPABILITY.orEmpty(cap, holder);
        }
        return super.getCapability(cap, side);
    }
    // endregion
}
