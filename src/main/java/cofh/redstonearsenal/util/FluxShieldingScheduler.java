package cofh.redstonearsenal.util;

import cofh.core.compat.curios.CuriosProxy;
import cofh.redstonearsenal.client.renderer.FluxShieldingHUDRenderer;
import cofh.redstonearsenal.network.client.FluxShieldingPacket;
import com.google.common.collect.HashMultimap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

import static cofh.redstonearsenal.capability.CapabilityFluxShielding.FLUX_SHIELDED_ITEM_CAPABILITY;

public class FluxShieldingScheduler {

    public static HashMultimap<Long, ServerPlayerEntity> schedule = HashMultimap.create();

    public static void updateHUD(ServerPlayerEntity player) {

        FluxShieldingPacket.sendToClient(FluxShieldingHelper.countCharges(player), player);
    }

    public static void updateHUD(int currCharges, int maxCharges, ServerPlayerEntity player) {

        FluxShieldingPacket.sendToClient(currCharges, maxCharges, player);
    }

    @OnlyIn(Dist.CLIENT)
    public static void updateHUD(int currCharges, int maxCharges) {

        FluxShieldingHUDRenderer.currCharges = currCharges;
        FluxShieldingHUDRenderer.maxCharges = maxCharges;
    }

    public static void scheduleUpdate(long time, ServerPlayerEntity player) {

        schedule.put(time, player);
    }

    public static void loadSchedule(long time, ServerPlayerEntity player) {

        Consumer<ItemStack> schedule = i -> i.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).ifPresent(c -> c.scheduleUpdate(player, time));

        //ARMOR
        for (ItemStack item : player.getAllSlots()) {
            schedule.accept(item);
        }
        //CURIOS
        CuriosProxy.getAllWorn(player).ifPresent(c -> {
            for (int i = 0; i < c.getSlots(); ++i) {
                schedule.accept(c.getStackInSlot(i));
            }
        });
    }

    public static void handleSchedule(long time) {

        if (schedule.containsKey(time)) {
            for (ServerPlayerEntity player : schedule.get(time)) {
                updateHUD(player);
            }
            schedule.removeAll(time);
        }
    }
}
