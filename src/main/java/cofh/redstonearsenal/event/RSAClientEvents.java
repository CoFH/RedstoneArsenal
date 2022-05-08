package cofh.redstonearsenal.event;

import cofh.core.network.packet.server.ItemLeftClickPacket;
import cofh.core.util.ProxyUtils;
import cofh.redstonearsenal.client.renderer.FluxShieldingHUDRenderer;
import cofh.redstonearsenal.item.FluxCrossbowItem;
import cofh.redstonearsenal.item.FluxSwordItem;
import cofh.redstonearsenal.item.FluxTridentItem;
import cofh.redstonearsenal.util.FluxShieldingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

@Mod.EventBusSubscriber (value = Dist.CLIENT, modid = ID_REDSTONE_ARSENAL)
public class RSAClientEvents {

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleClickInputEvent(ClickInputEvent event) {

        if (event.isCanceled() || !event.isAttack()) {
            return;
        }
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        // Flux Sword
        if (stack.getItem() instanceof FluxSwordItem) {
            ItemLeftClickPacket.createAndSend();
        }
        // Flux Trident
        if (stack.getItem() instanceof FluxTridentItem) {
            FluxTridentItem trident = (FluxTridentItem) stack.getItem();
            if (trident.isEmpowered(stack) && trident.hasEnergy(stack, true) && trident.startPlunge(player)) {
                event.setCanceled(true);
                event.setSwingHand(false);
                ItemLeftClickPacket.createAndSend();
            }
        }
        // Flux Crossbow
        if (stack.getItem() instanceof FluxCrossbowItem && ((FluxCrossbowItem) stack.getItem()).getLoadedAmmoCount(stack) > 0) {
            RayTraceResult result = Minecraft.getInstance().hitResult;
            if (result == null || !result.getType().equals(RayTraceResult.Type.BLOCK)) {
                ItemLeftClickPacket.createAndSend();
            } else if (player.attackStrengthTicker > 5) {
                ItemLeftClickPacket.createAndSend();
                player.resetAttackStrengthTicker();
            }
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void onDrawScreenPost(RenderGameOverlayEvent.Post event) {

        // Flux Shielding
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && Minecraft.getInstance().gameMode.canHurtPlayer()) {
            FluxShieldingHUDRenderer.render(event.getMatrixStack());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        // Flux Shielding
        if (event.phase == TickEvent.Phase.END && ProxyUtils.isClient()) {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null && (player.level.getGameTime() & 7) == 0) {
                FluxShieldingHelper.updateHUD(player);
            }
        }
    }

}
