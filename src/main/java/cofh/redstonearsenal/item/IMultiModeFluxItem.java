package cofh.redstonearsenal.item;

import cofh.core.util.helpers.ChatHelper;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.item.IMultiModeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public interface IMultiModeFluxItem extends IFluxItem, IMultiModeItem {

    default boolean isEmpowered(ItemStack stack) {

        return getMode(stack) > 0;
    }

    default float getEmpoweredModelProperty(ItemStack stack, Level world, LivingEntity entity, int seed) {

        return getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F;
    }

    // region IMultiModeItem
    @Override
    default void onModeChange(Player player, ItemStack stack) {

        if (isEmpowered(stack)) {
            player.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.4F, 1.0F);
        } else {
            player.level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F, 0.6F);
        }
        ChatHelper.sendIndexedChatMessageToPlayer(player, new TranslatableComponent("info.redstone_arsenal.mode." + getMode(stack)));
    }
    // endregion

    @Override
    default ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {

        return new EnergyContainerItemWrapper(stack, this, getEnergyCapability());
    }

    default void tooltipDelegate(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        if (isEmpowered(stack)) {
            tooltip.add(getTextComponent("info.redstone_arsenal.mode.1").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(getTextComponent("info.redstone_arsenal.mode.0").withStyle(ChatFormatting.GRAY));
        }
        if (getNumModes(stack) > 1) {
            addModeChangeTooltip(this, stack, worldIn, tooltip, flagIn);
        }

        IFluxItem.super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

}
