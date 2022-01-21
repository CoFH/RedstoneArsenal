package cofh.redstonearsenal.capability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

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

    /**
     * Schedules an update to the HUD. Automatically called after a charge is successfully used.
     * May be kept empty if no update is necessary.
     *
     * @param player   Player with the item equipped.
     * @param currTime The current world time.
     */
    void scheduleUpdate(ServerPlayerEntity player, long currTime);

    default void scheduleUpdate(ServerPlayerEntity player) {

        scheduleUpdate(player, player.level.getGameTime());
    }

}
