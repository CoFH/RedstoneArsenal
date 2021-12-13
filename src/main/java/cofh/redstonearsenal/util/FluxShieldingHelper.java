package cofh.redstonearsenal.util;

import cofh.core.compat.curios.CuriosProxy;
import cofh.redstonearsenal.capability.IFluxShieldedItem;
import cofh.redstonearsenal.client.renderer.FluxShieldingHUDRenderer;
import cofh.redstonearsenal.network.client.FluxShieldingPacket;
import com.google.common.collect.HashMultimap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static cofh.redstonearsenal.capability.CapabilityFluxShielding.FLUX_SHIELDED_ITEM_CAPABILITY;

public class FluxShieldingHelper {

    public static final String TAG_FLUX_SHIELD = "FluxShield";
    public static HashMultimap<Long, ServerPlayerEntity> updateSchedule = HashMultimap.create();

    public static void updateHUD(ServerPlayerEntity player) {

        FluxShieldingPacket.sendToClient(FluxShieldingHelper.countCharges(player), player);
    }

    public static void updateHUD(int currCharges, int maxCharges, ServerPlayerEntity player) {

        FluxShieldingPacket.sendToClient(currCharges, maxCharges, player);
    }

    @OnlyIn (Dist.CLIENT)
    public static void updateHUD(int currCharges, int maxCharges) {

        FluxShieldingHUDRenderer.currCharges = currCharges;
        FluxShieldingHUDRenderer.maxCharges = maxCharges;
    }

    public static void scheduleHUDUpdate(long time, ServerPlayerEntity player) {

        updateSchedule.put(time, player);
    }

    public static void handleHUDSchedule(long time) {

        if (updateSchedule.containsKey(time)) {
            for (ServerPlayerEntity player : updateSchedule.get(time)) {
                updateHUD(player);
            }
            updateSchedule.removeAll(time);
        }
    }

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

        SoundCategory category = SoundCategory.NEUTRAL;
        if (entity instanceof PlayerEntity) {
            category = SoundCategory.PLAYERS;
        } else if (entity instanceof MonsterEntity) {
            category = SoundCategory.HOSTILE;
        }
        entity.level.playSound(null, entity.blockPosition(), SoundEvents.ITEM_BREAK, category, 1.0F, 1.0F); //TODO: sound event

        AxisAlignedBB bounds = entity.getBoundingBox();
        Vector3d pos = bounds.getCenter();
        if (!entity.level.isClientSide()) {
            ((ServerWorld) entity.level).sendParticles(RedstoneParticleData.REDSTONE, pos.x(), pos.y(), pos.z(), 20, bounds.getXsize() * 0.5 + 0.2, bounds.getYsize() * 0.5 + 0.2, bounds.getZsize() * 0.5 + 0.2, 0);
        }
    }

}
