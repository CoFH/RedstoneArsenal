package cofh.redstonearsenal.item;

import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.item.ICoFHItem;
import cofh.lib.item.IMultiModeItem;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.MathHelper;
import cofh.redstonearsenal.util.RSAEnergyHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.item.ContainerType.ENERGY;
import static cofh.lib.util.constants.Constants.RGB_DURABILITY_FLUX;
import static cofh.lib.util.helpers.StringHelper.*;

public interface IFluxItem extends ICoFHItem, IEnergyContainerItem, IMultiModeItem {

    int ENERGY_PER_USE = 200;
    int ENERGY_PER_USE_EMPOWERED = 800;

    default Capability<? extends IEnergyStorage> getEnergyCapability() {

        return RSAEnergyHelper.getBaseEnergySystem();
    }

    default boolean isEmpowered(ItemStack stack) {

        return getMode(stack) > 0;
    }

    default int getEnergyPerUse(boolean empowered) {

        return empowered ? ENERGY_PER_USE_EMPOWERED : ENERGY_PER_USE;
    }

    default boolean hasEnergy(ItemStack stack, int amount) {

        return getEnergyStored(stack) >= amount;
    }

    default boolean hasEnergy(ItemStack stack, boolean empowered) {

        return hasEnergy(stack, getEnergyPerUse(empowered));
    }

    default boolean useEnergy(ItemStack stack, int amount, boolean simulate) {

        if (simulate) {
            return true;
        }
        if (hasEnergy(stack, amount)) {
            extractEnergy(stack, amount, false);
            return true;
        }
        return false;
    }

    default boolean useEnergy(ItemStack stack, int amount, Entity entity) {

        return useEnergy(stack, amount, Utils.isCreativePlayer(entity));
    }

    default boolean useEnergy(ItemStack stack, boolean empowered, boolean simulate) {

        return useEnergy(stack, getEnergyPerUse(empowered), simulate);
    }

    default boolean useEnergy(ItemStack stack, boolean empowered, Entity entity) {

        return useEnergy(stack, getEnergyPerUse(empowered), Utils.isCreativePlayer(entity));
    }

    @Override
    default boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {

        return !(newStack.getItem() == oldStack.getItem()) || (getEnergyStored(oldStack) > 0 != getEnergyStored(newStack) > 0);
    }

    @Override
    default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {

        return !oldStack.equals(newStack) && (slotChanged || getEnergyStored(oldStack) > 0 != getEnergyStored(newStack) > 0);
    }

    @Override
    default boolean showDurabilityBar(ItemStack stack) {

        return getEnergyStored(stack) > 0;
    }

    @Override
    default int getRGBDurabilityForDisplay(ItemStack stack) {

        return RGB_DURABILITY_FLUX;
    }

    @Override
    default double getDurabilityForDisplay(ItemStack stack) {

        if (stack.getTag() == null) {
            return 0;
        }
        return MathHelper.clamp(1.0D - getEnergyStored(stack) / (double) getMaxEnergyStored(stack), 0.0D, 1.0D);
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

        boolean creative = isCreative(stack, ENERGY);
        tooltip.add(getTextComponent(localize("info.cofh.energy") + ": "
                + (creative ?
                localize("info.cofh.infinite") :
                getScaledNumber(getEnergyStored(stack)) + " / " + getScaledNumber(getMaxEnergyStored(stack)) + " RF")));
        addEnergyTooltip(stack, worldIn, tooltip, flagIn, getExtract(stack), getReceive(stack), creative);
    }

    static DamageSource fluxDirectDamage(LivingEntity attacker) {

        return (new EntityDamageSource("flux", attacker)).bypassArmor();
    }

    static DamageSource fluxRangedDamage(ProjectileEntity projectile, @Nullable Entity shooter) {

        return (new IndirectEntityDamageSource("flux", projectile, shooter)).setProjectile().bypassArmor();
    }

}
