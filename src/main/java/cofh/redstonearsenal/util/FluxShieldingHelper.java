package cofh.redstonearsenal.util;

import cofh.core.compat.curios.CuriosProxy;
import cofh.core.util.ProxyUtils;
import cofh.lib.util.helpers.MathHelper;
import cofh.redstonearsenal.common.capability.IFluxShieldedItem;
import cofh.redstonearsenal.common.network.packet.client.FluxShieldingPacket;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static cofh.redstonearsenal.common.capability.CapabilityFluxShielding.FLUX_SHIELDED_ITEM_CAPABILITY;
import static cofh.redstonearsenal.init.registries.ModSounds.SOUND_SHIELDING_BREAK;
import static cofh.redstonearsenal.init.registries.ModSounds.SOUND_SHIELDING_RECHARGE;

public class FluxShieldingHelper {

    public static final String TAG_FLUX_SHIELD = "FluxShield";

    public static int currentCharges = 0;
    public static int maximumCharges = 0;

    public static ItemStack findShieldedItem(LivingEntity entity) {

        Predicate<ItemStack> isShieldedItem = i -> i.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).map(cap -> cap.currCharges(entity) > 0).orElse(false);

        // ARMOR
        for (ItemStack piece : entity.getArmorSlots()) {
            if (isShieldedItem.test(piece)) {
                return piece;
            }
        }
        // CURIOS
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

        final int[] counter = {0, 0};
        if (entity == null) {
            return counter;
        }
        Consumer<ItemStack> count = i -> {
            counter[0] += getCurrCharges(entity, i);
            counter[1] += getMaxCharges(entity, i);
        };

        // ARMOR
        for (ItemStack item : entity.getArmorSlots()) {
            count.accept(item);
        }
        // CURIOS
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
        LazyOptional<IFluxShieldedItem> cap = stack.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY);
        if (cap.map(c -> c.useCharge(entity)).orElse(false)) {
            onUseFluxShieldCharge(entity);
            return true;
        }
        return false;
    }

    public static int getCurrCharges(LivingEntity entity, ItemStack stack) {

        return stack.isEmpty() ? 0 : stack.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).map(cap -> cap.currCharges(entity)).orElse(0);
    }

    public static int getMaxCharges(LivingEntity entity, ItemStack stack) {

        return stack.isEmpty() ? 0 : stack.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).map(cap -> cap.maxCharges(entity)).orElse(0);
    }

    public static boolean equalCharges(LivingEntity entity, ItemStack a, ItemStack b) {

        return getCurrCharges(entity, a) == getCurrCharges(entity, b) && getMaxCharges(entity, a) == getMaxCharges(entity, b);
    }

    protected static void onUseFluxShieldCharge(LivingEntity entity) {

        entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SOUND_SHIELDING_BREAK.get(), entity.getSoundSource(), 1.5F, 0.65F + MathHelper.RANDOM.nextFloat() * 0.2F);
        AABB bounds = entity.getBoundingBox();
        Vec3 pos = bounds.getCenter();
        if (!entity.level.isClientSide) {
            ((ServerLevel) entity.level).sendParticles(DustParticleOptions.REDSTONE, pos.x(), pos.y(), pos.z(), 20, bounds.getXsize() * 0.5 + 0.2, bounds.getYsize() * 0.5 + 0.2, bounds.getZsize() * 0.5 + 0.2, 0);
        }
    }

    public static void updateHUD(int currCharges, int maxCharges) {

        Player player = ProxyUtils.getClientPlayer();
        Level level = ProxyUtils.getClientWorld();
        if (maxCharges - currCharges < maximumCharges - currentCharges) {
            if (level != null && player != null) {
                level.playSound(player, player, SOUND_SHIELDING_RECHARGE.get(), SoundSource.PLAYERS, 0.75F, 0.3F * currCharges / maxCharges + 0.7F);
            }
        }
        currentCharges = currCharges;
        maximumCharges = maxCharges;
    }

    public static void updateHUD(int[] charges) {

        updateHUD(charges[0], charges[1]);
    }

    public static void updateHUD(Player player) {

        updateHUD(countCharges(player));
    }

    public static void updateHUD(ServerPlayer player) {

        FluxShieldingPacket.sendToClient(countCharges(player), player);
    }

}
