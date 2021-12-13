package cofh.redstonearsenal.item;

import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.item.IMultiModeItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public interface IMultiModeFluxItem extends IFluxItem, IMultiModeItem {

    default boolean isEmpowered(ItemStack stack) {

        return getMode(stack) > 0;
    }

    default float getEmpoweredModelProperty(ItemStack stack, World world, LivingEntity entity) {

        return getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F;
    }

    // region IMultiModeItem
    @Override
    default void onModeChange(PlayerEntity player, ItemStack stack) {

        if (isEmpowered(stack)) {
            player.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.4F, 1.0F);
        } else {
            player.level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.6F);
        }
    }
    // endregion

    @Override
    default ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new EnergyContainerItemWrapper(stack, this, getEnergyCapability());
    }

    default void tooltipDelegate(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        if (isEmpowered(stack)) {
            tooltip.add(getTextComponent("info.redstone_arsenal.empowered").withStyle(TextFormatting.RED));
        }
        if (getNumModes(stack) > 1) {
            addIncrementModeChangeTooltip(stack, worldIn, tooltip, flagIn);
        }

        IFluxItem.super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

}
