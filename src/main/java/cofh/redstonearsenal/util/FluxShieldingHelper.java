package cofh.redstonearsenal.util;

import cofh.core.compat.curios.CuriosProxy;
import cofh.redstonearsenal.capability.IFluxShieldedItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static cofh.redstonearsenal.capability.CapabilityFluxShielding.FLUX_SHIELDED_ITEM_CAPABILITY;

public class FluxShieldingHelper {

    public static ItemStack findShieldedItem(LivingEntity entity) {

        Predicate<ItemStack> isShieldedItem = i -> i.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).map(cap -> cap.availableCharges(entity) > 0).orElse(false);

        // HELD
        ItemStack mainHand = entity.getMainHandItem();
        if (isShieldedItem.test(mainHand)) {
            return mainHand;
        }
        ItemStack offHand = entity.getOffhandItem();
        if (isShieldedItem.test(offHand)) {
            return offHand;
        }
        //ARMOR
        for (ItemStack piece : entity.getArmorSlots()) {
            if (isShieldedItem.test(piece)) {
                return piece;
            }
        }
        //CURIOS
        final ItemStack[] retStack = {ItemStack.EMPTY};
        CuriosProxy.getAllWorn(entity).ifPresent(c -> {
            for (int i = 0; i < c.getSlots(); ++i) {
                ItemStack slot = c.getStackInSlot(i);
                if (isShieldedItem.test(slot)) {
                    retStack[0] = slot;
                    return;
                }
            }
        });
        return retStack[0];
    }

    public static int[] countCharges(LivingEntity entity) {

        if (entity == null) {
            return new int[]{0, 0};
        }
        final int[] counter = {0, 0};
        Consumer<ItemStack> count = i -> {
            LazyOptional<IFluxShieldedItem> cap = i.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY);
            counter[0] += cap.map(c -> c.availableCharges(entity)).orElse(0);
            counter[1] += cap.map(c -> c.maxCharges(entity)).orElse(0);
        };

        //HELD
        count.accept(entity.getMainHandItem());
        count.accept(entity.getOffhandItem());
        //ARMOR
        for (ItemStack piece : entity.getArmorSlots()) {
            count.accept(piece);
        }
        //CURIOS
        CuriosProxy.getAllWorn(entity).ifPresent(c -> {
            for (int i = 0; i < c.getSlots(); ++i) {
                count.accept(c.getStackInSlot(i));
            }
        });
        return counter;
    }

    public static boolean hasFluxShieldCharge(LivingEntity entity) {

        return !findShieldedItem(entity).isEmpty();
    }

    public static boolean useFluxShieldCharge(LivingEntity entity) {

        return useFluxShieldCharge(entity, findShieldedItem(entity));
    }

    public static boolean useFluxShieldCharge(LivingEntity entity, ItemStack stack) {

        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).map(cap -> cap.useCharge(entity)).orElse(false)) {
            onUseFluxShieldCharge(entity);
            return true;
        }
        return false;
    }

    protected static void onUseFluxShieldCharge(LivingEntity entity) {

        entity.level.playSound(null, entity.blockPosition(), SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F); //TODO: soundcategory
        Random random = entity.getRandom();
        for (int i = 0; i < 20; ++i) { //TODO: fix particles
            entity.level.addParticle(RedstoneParticleData.REDSTONE, entity.getX(), entity.getY(), entity.getZ(), random.nextFloat() - 0.5, random.nextFloat() - 0.5, random.nextFloat() - 0.5);
        }
    }

}
