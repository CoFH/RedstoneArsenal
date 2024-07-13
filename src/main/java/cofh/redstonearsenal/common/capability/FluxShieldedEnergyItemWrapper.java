package cofh.redstonearsenal.common.capability;

import cofh.lib.api.item.IEnergyContainerItem;
import cofh.lib.common.energy.EnergyContainerItemWrapper;
import cofh.lib.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import static cofh.redstonearsenal.util.FluxShieldingHelper.TAG_FLUX_SHIELD;

/**
 * Standard implementation for the IFluxShieldItem capability.
 * An energy container with 1 shield charge that takes 30 seconds to regenerate.
 *
 * @author Hekera
 * why am i here
 */
public class FluxShieldedEnergyItemWrapper extends EnergyContainerItemWrapper implements IFluxShieldedItem {

    protected final ItemStack shieldedItem;
    protected final int COOLDOWN = 600;
    protected int energyPerUse;
    protected long availableTime = -1;

    public FluxShieldedEnergyItemWrapper(ItemStack shieldedItemContainer, int energyPerUse) {

        super(shieldedItemContainer, (IEnergyContainerItem) shieldedItemContainer.getItem());
        this.shieldedItem = shieldedItemContainer;
        this.energyPerUse = energyPerUse;
    }

    @Override
    public int curCharges(LivingEntity entity) {

        CompoundTag nbt = shieldedItem.getOrCreateTag();
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

        if (curCharges(entity) < 1 || (energyPerUse > 0 && getEnergyStored() >= energyPerUse && extractEnergy(energyPerUse, Utils.isCreativePlayer(entity)) != energyPerUse)) {
            return false;
        }
        availableTime = entity.level.getGameTime() + COOLDOWN;
        shieldedItem.getOrCreateTag().putLong(TAG_FLUX_SHIELD, availableTime);
        return true;
    }

}
