package cofh.redstonearsenal.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (FishingHook.class)
public abstract class FishingHookMixin {

    @Inject (
            method = "shouldStopFishing",
            at = @At (value = "HEAD"),
            cancellable = true
    )
    private static void onShouldStopFishing(Player player, CallbackInfoReturnable<Boolean> callback) {

        FishingHook hook = player.fishing;
        if (hook == null) {
            return;
        }
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offhandItem = player.getOffhandItem();
        boolean flag = mainHandItem.getItem() instanceof FishingRodItem;
        boolean flag1 = offhandItem.getItem() instanceof FishingRodItem;
        if (!player.isRemoved() && player.isAlive() && (flag || flag1) && !(hook.distanceToSqr(player) > 1024.0D)) {
            callback.setReturnValue(false);
        } else {
            hook.discard();
            callback.setReturnValue(true);
        }
        callback.cancel();
    }

}