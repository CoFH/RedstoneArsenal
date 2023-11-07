package cofh.redstonearsenal.common.capability;

import net.minecraft.world.entity.LivingEntity;

/**
 * Implement this interface as a capability for an equippable item with a flux shield.
 * Flux shields use up a charge to negate a single instance of damage.
 * Charges regenerate under certain circumstances, such as a certain period of time passing.
 *
 * @author Hekera
 * why am i here
 */

public interface IFluxShieldedItem {

    /**
     * @param entity Entity with the item equipped.
     * @return Number of charges available to use.
     */
    int currCharges(LivingEntity entity);

    /**
     * @param entity Entity with the item equipped.
     * @return Maximum number of charges.
     */
    int maxCharges(LivingEntity entity);

    /**
     * Uses up a charge.
     *
     * @param entity Entity with the item equipped.
     * @return Returns false if there are no charges to use or the operation otherwise fails.
     */
    boolean useCharge(LivingEntity entity);

}
