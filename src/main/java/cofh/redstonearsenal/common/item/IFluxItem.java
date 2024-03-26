package cofh.redstonearsenal.common.item;

import cofh.lib.api.item.ICoFHItem;
import cofh.lib.api.item.IEnergyContainerItem;
import cofh.lib.common.energy.EnergyContainerItemWrapper;
import cofh.lib.util.Utils;
import cofh.redstonearsenal.util.RSAEnergyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static cofh.lib.api.ContainerType.ENERGY;
import static cofh.lib.util.Constants.RGB_DURABILITY_FLUX;
import static cofh.lib.util.helpers.StringHelper.*;
import static cofh.redstonearsenal.RedstoneArsenal.FLUX;
import static cofh.redstonearsenal.RedstoneArsenal.FLUX_RANGED;

public interface IFluxItem extends ICoFHItem, IEnergyContainerItem {

    int ENERGY_PER_USE = 500;
    int ENERGY_PER_USE_EMPOWERED = 2000;

    default Capability<? extends IEnergyStorage> getEnergyCapability() {

        return RSAEnergyHelper.getBaseEnergySystem();
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

    default boolean useEnergy(ItemStack stack, boolean empowered, @Nullable Entity entity) {

        return useEnergy(stack, getEnergyPerUse(empowered), Utils.isCreativePlayer(entity));
    }

    @Override
    default boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {

        return (newStack.getItem() != oldStack.getItem()) || (getEnergyStored(oldStack) > 0 != getEnergyStored(newStack) > 0);
    }

    @Override
    default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {

        return !oldStack.equals(newStack) && (slotChanged || getEnergyStored(oldStack) > 0 != getEnergyStored(newStack) > 0);
    }

    @Override
    default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {

        useEnergy(stack, Math.min(getEnergyStored(stack), amount * getEnergyPerUse(false)), entity);
        return 0;
    }

    default boolean isBarVisible(ItemStack stack) {

        return getEnergyStored(stack) > 0;
    }

    default int getBarColor(ItemStack stack) {

        return RGB_DURABILITY_FLUX;
    }

    default int getBarWidth(ItemStack stack) {

        if (stack.getTag() == null) {
            return 0;
        }
        return (int) Math.round(13.0D * getEnergyStored(stack) / (double) getMaxEnergyStored(stack));
    }

    default float getChargedModelProperty(ItemStack stack, Level world, LivingEntity entity, int seed) {

        return hasEnergy(stack, false) ? 1F : 0F;
    }

    @Override
    default ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {

        return new EnergyContainerItemWrapper(stack, this, getEnergyCapability());
    }

    default void tooltipDelegate(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        boolean creative = isCreative(stack, ENERGY);
        if (getMaxEnergyStored(stack) > 0) {
            tooltip.add(creative
                    ? getTextComponent(localize("info.cofh.energy") + ": ").append(getTextComponent("info.cofh.infinite").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC))
                    : getTextComponent(localize("info.cofh.energy") + ": " + getScaledNumber(getEnergyStored(stack)) + " / " + getScaledNumber(getMaxEnergyStored(stack)) + " " + localize("info.cofh.unit_rf")));
        }
        addEnergyTooltip(stack, worldIn, tooltip, flagIn, getExtract(stack), getReceive(stack), creative);
    }

    static DamageSource fluxDirectDamage(LivingEntity attacker) {

        return attacker.level.damageSources().source(FLUX, attacker);
    }

    static DamageSource fluxRangedDamage(Projectile projectile, @Nullable Entity shooter) {

        return projectile.level.damageSources().source(FLUX_RANGED, shooter, projectile);
    }

}
