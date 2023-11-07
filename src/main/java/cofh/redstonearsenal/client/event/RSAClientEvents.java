package cofh.redstonearsenal.client.event;

import cofh.core.common.network.packet.server.ItemLeftClickPacket;
import cofh.core.util.ProxyUtils;
import cofh.redstonearsenal.common.item.FluxCrossbowItem;
import cofh.redstonearsenal.common.item.FluxSickleItem;
import cofh.redstonearsenal.common.item.FluxSwordItem;
import cofh.redstonearsenal.common.item.FluxTridentItem;
import cofh.redstonearsenal.util.FluxShieldingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

@Mod.EventBusSubscriber (value = Dist.CLIENT, modid = ID_REDSTONE_ARSENAL)
public class RSAClientEvents {

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleClickInputEvent(InteractionKeyMappingTriggered event) {

        if (event.isCanceled() || !event.isAttack()) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        Item item = stack.getItem();
        // Flux Sword and Sickle
        if (item instanceof FluxSwordItem || item instanceof FluxSickleItem) {
            ItemLeftClickPacket.createAndSend();
        }
        // Flux Trident
        if (item instanceof FluxTridentItem trident) {
            if (trident.isEmpowered(stack) && trident.hasEnergy(stack, true) && trident.startPlunge(player)) {
                event.setCanceled(true);
                event.setSwingHand(false);
                ItemLeftClickPacket.createAndSend();
            }
        }
        // Flux Crossbow
        if (item instanceof FluxCrossbowItem crossbow && crossbow.getLoadedAmmoCount(stack) > 0) {
            HitResult result = Minecraft.getInstance().hitResult;
            if (result == null || !result.getType().equals(HitResult.Type.BLOCK)) {
                ItemLeftClickPacket.createAndSend();
            } else if (player.attackStrengthTicker > 5) {
                ItemLeftClickPacket.createAndSend();
                player.resetAttackStrengthTicker();
            }
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        // Flux Shielding
        if (event.phase == TickEvent.Phase.END && ProxyUtils.isClient()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && (player.level.getGameTime() & 7) == 0) {
                FluxShieldingHelper.updateHUD(player);
            }
        }
    }


    //@SubscribeEvent
    //public static void renderHandEvent(RenderHandEvent event) {
    //
    //    // Flux Crossbow
    //    ItemStack stack = event.getItemStack();
    //    if (!(stack.getItem() instanceof CrossbowItemCoFH)) {
    //        return;
    //    }
    //    event.setCanceled(true);
    //    Minecraft mc = Minecraft.getInstance();
    //    PoseStack poseStack = event.getPoseStack();
    //    InteractionHand hand = event.getHand();
    //    MultiBufferSource buffer = event.getMultiBufferSource();
    //    Player player = mc.player;
    //    float partialTicks = event.getPartialTicks();
    //    float equipProgress = event.getEquipProgress();
    //    float swing = event.getSwingProgress();
    //    int light = event.getPackedLight();
    //
    //    boolean isMain = hand == InteractionHand.MAIN_HAND;
    //    boolean isCharged = CrossbowItemCoFH.isCharged(stack);
    //    HumanoidArm arm = isMain ? player.getMainArm() : player.getMainArm().getOpposite();
    //    boolean isRight = arm == HumanoidArm.RIGHT;
    //    int i = (player.getMainArm() == HumanoidArm.RIGHT) == player.getMainHandItem().getItem() instanceof FishingRodItemCoFH ? 1 : -1;
    //
    //    if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
    //        applyItemArmTransform(poseStack, arm, equipProgress);
    //        poseStack.translate((float)i * -0.4785682F, -0.094387F, 0.05731531F);
    //        poseStack.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
    //        poseStack.mulPose(Vector3f.YP.rotationDegrees((float)i * 65.3F));
    //        poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)i * -9.785F));
    //        float f9 = (float)stack.getUseDuration() - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
    //        float f13 = f9 / (float)CrossbowItem.getChargeDuration(stack);
    //        if (f13 > 1.0F) {
    //            f13 = 1.0F;
    //        }
    //
    //        if (f13 > 0.1F) {
    //            float f16 = Mth.sin((f9 - 0.1F) * 1.3F);
    //            float f3 = f13 - 0.1F;
    //            float f4 = f16 * f3;
    //            poseStack.translate((double)(f4 * 0.0F), (double)(f4 * 0.004F), (double)(f4 * 0.0F));
    //        }
    //
    //        poseStack.translate((double)(f13 * 0.0F), (double)(f13 * 0.0F), (double)(f13 * 0.04F));
    //        poseStack.scale(1.0F, 1.0F, 1.0F + f13 * 0.2F);
    //        poseStack.mulPose(Vector3f.YN.rotationDegrees((float)i * 45.0F));
    //    } else {
    //        float f = -0.4F * Mth.sin(Mth.sqrt(swing) * (float)Math.PI);
    //        float f1 = 0.2F * Mth.sin(Mth.sqrt(swing) * ((float)Math.PI * 2F));
    //        float f2 = -0.2F * Mth.sin(swing * (float)Math.PI);
    //        poseStack.translate((double)((float)i * f), (double)f1, (double)f2);
    //        applyItemArmTransform(poseStack, arm, equipProgress);
    //        applyItemArmAttackTransform(poseStack, arm, swing);
    //        if (isCharged && swing < 0.001F && isMain) {
    //            poseStack.translate((double)((float)i * -0.641864F), 0.0D, 0.0D);
    //            poseStack.mulPose(Vector3f.YP.rotationDegrees((float)i * 10.0F));
    //        }
    //    }
    //
    //    mc.getItemInHandRenderer().renderItem(player, stack, isRight ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRight, poseStack, buffer, light);
    //
    //}
    //
    //private static void applyItemArmAttackTransform(PoseStack stack, HumanoidArm arm, float partialTicks) {
    //    int i = arm == HumanoidArm.RIGHT ? 1 : -1;
    //    float f = Mth.sin(partialTicks * partialTicks * (float)Math.PI);
    //    stack.mulPose(Vector3f.YP.rotationDegrees((float)i * (45.0F + f * -20.0F)));
    //    float f1 = Mth.sin(Mth.sqrt(partialTicks) * (float)Math.PI);
    //    stack.mulPose(Vector3f.ZP.rotationDegrees((float)i * f1 * -20.0F));
    //    stack.mulPose(Vector3f.XP.rotationDegrees(f1 * -80.0F));
    //    stack.mulPose(Vector3f.YP.rotationDegrees((float)i * -45.0F));
    //}
    //
    //private static void applyItemArmTransform(PoseStack stack, HumanoidArm p_109384_, float p_109385_) {
    //    int i = p_109384_ == HumanoidArm.RIGHT ? 1 : -1;
    //    stack.translate((double)((float)i * 0.56F), (double)(-0.52F + p_109385_ * -0.6F), (double)-0.72F);
    //}

}
