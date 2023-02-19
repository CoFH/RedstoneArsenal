package cofh.redstonearsenal.item;

import cofh.core.config.CoreClientConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.util.Utils;
import cofh.lib.util.constants.NBTTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public class FluxElytraItem extends FluxArmorItem implements IMultiModeFluxItem {

    public float propelSpeed = 0.85F;
    public float brakeRate = 0.95F;
    public int boostTime = 32;
    public int energyUseInterval = 8;

    public FluxElytraItem(ArmorMaterial material, EquipmentSlot slot, Properties builder, int maxEnergy, int maxTransfer) {

        super(material, slot, builder, maxEnergy, maxTransfer);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("empowered"), this::getEmpoweredModelProperty);
    }

    @Override

    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        if (Screen.hasShiftDown() || CoreClientConfig.alwaysShowDetails.get()) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreClientConfig.holdShiftForDetails.get()) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {

        return new EnergyContainerItemWrapper(stack, this, getEnergyCapability());
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {

        return hasEnergy(stack, false);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {

        boolean isCreative = Utils.isCreativePlayer(entity);
        boolean simulate = isCreative || flightTicks % energyUseInterval != 0;
        if (!useEnergy(stack, false, simulate)) {
            return false;
        }

        if (entity.isShiftKeyDown() && useEnergy(stack, true, simulate)) {
            stack.getOrCreateTag().remove(NBTTags.TAG_TIME);
            brake(entity);
        } else {
            CompoundTag tag = stack.getOrCreateTag();
            long time = entity.level.getGameTime();
            if (time - tag.getLong(NBTTags.TAG_TIME) <= boostTime) {
                propel(entity);
            } else if (isEmpowered(stack) && useEnergy(stack, true, isCreative)) {
                tag.putLong(NBTTags.TAG_TIME, time);
                propel(entity);
            }
        }

        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {

        if (entity instanceof LivingEntity && !((LivingEntity) entity).isFallFlying()) {
            stack.getOrCreateTag().remove(NBTTags.TAG_TIME);
        }
    }

    //Used to boost a single time, similar to a rocket.
    public boolean boost(ItemStack stack, LivingEntity entity, int time) {

        if (!entity.getItemBySlot(EquipmentSlot.CHEST).canElytraFly(entity)) {
            return false;
        }
        boolean isPlayer = entity instanceof Player;
        boolean isCreative = isPlayer && ((Player) entity).abilities.instabuild;
        if (!useEnergy(stack, getEnergyPerUse(true) * time / energyUseInterval, isCreative)) {
            return false;
        }
        if (!entity.isFallFlying() && isPlayer) {
            ((Player) entity).startFallFlying();
        }
        propel(entity, propelSpeed);
        stack.getOrCreateTag().putLong(NBTTags.TAG_TIME, entity.level.getGameTime());

        return true;
    }

    public boolean boost(ItemStack stack, LivingEntity entity) {

        return boost(stack, entity, boostTime);
    }

    public void propel(LivingEntity entity, double speed) {

        if (entity.isFallFlying()) {
            Vec3 look = entity.getLookAngle();
            Vec3 velocity = entity.getDeltaMovement();
            entity.setDeltaMovement(velocity.add(look.x * speed - velocity.x * 0.5, look.y * speed - velocity.y * 0.5, look.z * speed - velocity.z * 0.5));

            if (entity.level.isClientSide()) {
                entity.level.addParticle(DustParticleOptions.REDSTONE, entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
            }
        }
    }

    public void propel(LivingEntity entity) {

        propel(entity, propelSpeed);
    }

    public void brake(LivingEntity entity, double rate) {

        if (entity.isFallFlying()) {
            Vec3 velocity = entity.getDeltaMovement();
            double horzBrake = velocity.x() * velocity.x() + velocity.z() * velocity.z() > 0.16 ? rate : 1;
            double vertBrake = velocity.y() * velocity.y() > 0.2 ? rate : 1;
            entity.setDeltaMovement(velocity.multiply(horzBrake, vertBrake, horzBrake));
        }
    }

    public void brake(LivingEntity entity) {

        brake(entity, brakeRate);
    }

}
