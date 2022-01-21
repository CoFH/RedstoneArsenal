package cofh.redstonearsenal.capability;

import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.util.Utils;
import cofh.redstonearsenal.util.FluxShieldingScheduler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static cofh.redstonearsenal.capability.CapabilityFluxShielding.FLUX_SHIELDED_ITEM_CAPABILITY;
import static cofh.redstonearsenal.util.FluxShieldingHelper.TAG_FLUX_SHIELD;

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
    protected long availableTime = -1;

    public FluxShieldedEnergyItemWrapper(ItemStack shieldedItemContainer, int energyPerUse) {

        super(shieldedItemContainer, (IEnergyContainerItem) shieldedItemContainer.getItem(), ((IEnergyContainerItem) shieldedItemContainer.getItem()).getEnergyCapability());
        this.shieldedItem = shieldedItemContainer;
        this.energyPerUse = energyPerUse;
    }

    @Override
    public int currCharges(LivingEntity entity) {

        CompoundNBT nbt = shieldedItem.getOrCreateTag();
        if (energyPerUse > 0 && getEnergyStored() < energyPerUse) {
            return 0;
        }
        if (availableTime <= -1) {
            if (!nbt.contains(TAG_FLUX_SHIELD)) {
                availableTime = 0;
                return 1;
            }
            availableTime = nbt.getLong(TAG_FLUX_SHIELD);
        }
        return entity.level.getGameTime() >= availableTime ? 1 : 0;
    }

    @Override
    public int maxCharges(LivingEntity entity) {

        return 1;
    }

    @Override
    public boolean useCharge(LivingEntity entity) {

        if (currCharges(entity) < 1 || (energyPerUse > 0 && getEnergyStored() >= energyPerUse && extractEnergy(energyPerUse, Utils.isCreativePlayer(entity)) != energyPerUse)) {
            return false;
        }
        availableTime = entity.level.getGameTime() + COOLDOWN;
        shieldedItem.getOrCreateTag().putLong(TAG_FLUX_SHIELD, availableTime);
        return true;
    }

    @Override
    public void scheduleUpdate(ServerPlayerEntity player, long currTime) {

        if (availableTime <= -1) {
            CompoundNBT nbt = shieldedItem.getOrCreateTag();
            if (!nbt.contains(TAG_FLUX_SHIELD)) {
                availableTime = 0;
            } else {
                availableTime = nbt.getLong(TAG_FLUX_SHIELD);
            }
        }
        if (availableTime > currTime) {
            FluxShieldingScheduler.scheduleUpdate(availableTime, player);
        }
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
