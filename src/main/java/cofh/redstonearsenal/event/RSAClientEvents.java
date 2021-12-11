package cofh.redstonearsenal.event;

import cofh.core.network.packet.server.ItemLeftClickPacket;
import cofh.redstonearsenal.client.renderer.FluxShieldingHUDRenderer;
import cofh.redstonearsenal.item.FluxSwordItem;
import cofh.redstonearsenal.item.FluxTridentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ID_REDSTONE_ARSENAL)
public class RSAClientEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
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
        if (stack.getItem() instanceof FluxSwordItem) {
            ItemLeftClickPacket.createAndSend();
        }
        if (stack.getItem() instanceof FluxTridentItem) {
            FluxTridentItem trident = (FluxTridentItem) stack.getItem();
            if (trident.isEmpowered(stack) && trident.hasEnergy(stack, true) && trident.startPlunge(player)) {
                event.setCanceled(true);
                ItemLeftClickPacket.createAndSend();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrawScreenPost(RenderGameOverlayEvent.Post event) {

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && Minecraft.getInstance().gameMode.canHurtPlayer()) {
            FluxShieldingHUDRenderer.render(event.getMatrixStack());
        }
    }

}
