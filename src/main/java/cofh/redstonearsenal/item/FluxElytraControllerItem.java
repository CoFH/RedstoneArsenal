package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.lib.item.ICoFHItem;
import cofh.lib.item.IMultiModeItem;
import cofh.lib.util.helpers.SecurityHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static cofh.lib.item.ContainerType.ENERGY;
import static cofh.lib.util.helpers.StringHelper.*;
import static cofh.lib.util.helpers.StringHelper.getScaledNumber;
import static net.minecraft.util.text.TextFormatting.*;

public class FluxElytraControllerItem extends Item implements ICoFHItem, IMultiModeItem {

    public FluxElytraControllerItem(Item.Properties builder) {

        super(builder);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        if (isEmpowered(stack)) {
            tooltip.add(getTextComponent("Empowered")); //TODO: localize or remove
        }
    }

    public boolean isEmpowered(ItemStack stack) {

        return getMode(stack) > 0;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        ItemStack chest = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (chest.getItem() instanceof FluxElytraItem) {
            FluxElytraItem elytra = (FluxElytraItem) chest.getItem();
            elytra.setMode(chest, getMode(stack));

            if (!isEmpowered(stack)) {
                if (elytra.boost(chest, player)) {
                    return ActionResult.sidedSuccess(stack, world.isClientSide());
                }
            }
            else if (player.isFallFlying()) {
                elytra.setPropelTime(-1);
                player.startUsingItem(hand);
                return ActionResult.consume(stack);
            }
        }
        return ActionResult.pass(stack);
    }

    @Override
    public void onUseTick(World world, LivingEntity living, ItemStack stack, int useDuration) {

        if (!living.isFallFlying() || !isEmpowered(stack)) {
            living.releaseUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity living, int useDuration) {

        ItemStack chest = living.getItemBySlot(EquipmentSlotType.CHEST);
        if (chest.getItem() instanceof FluxElytraItem) {
            ((FluxElytraItem) chest.getItem()).setPropelTime(0);
        }
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {

        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack p_77626_1_) {

        return 72000;
    }

    @Override
    public void onModeChange(PlayerEntity player, ItemStack stack) {

        if (isEmpowered(stack)) {
            player.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.4F, 1.0F);
        } 
        else {
            player.level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.6F);
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (chest.getItem() instanceof FluxElytraItem) {
            ((FluxElytraItem) chest.getItem()).setMode(chest, getMode(stack));
        }
    }
}
