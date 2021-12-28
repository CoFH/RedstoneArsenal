package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.util.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.GRAY;

public class FluxElytraItem extends FluxArmorItem implements IMultiModeFluxItem {

    public float propelSpeed = 0.85F;
    public float brakeRate = 0.95F;
    public int boostTime = 32;
    public int energyUseInterval = 8;

    protected int propelTime = 0;

    public FluxElytraItem(IArmorMaterial material, EquipmentSlotType slot, Properties builder, int maxEnergy, int maxTransfer) {

        super(material, slot, builder, maxEnergy, maxTransfer);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("empowered"), this::getEmpoweredModelProperty);
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        if (Screen.hasShiftDown() || CoreConfig.alwaysShowDetails) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreConfig.holdShiftForDetails) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(GRAY));
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new EnergyContainerItemWrapper(stack, this, getEnergyCapability());
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {

        return hasEnergy(stack, false);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {

        boolean isCreative = Utils.isCreativePlayer(entity);
        boolean shouldExtract = flightTicks % energyUseInterval == 0 && !isCreative;
        if (!useEnergy(stack, false, !shouldExtract)) {
            return false;
        }

        if (entity.isShiftKeyDown() && useEnergy(stack, true, !shouldExtract)) {
            propelTime = 0;
            brake(entity);
        } else if (propelTime > 0) {
            propel(entity);
            --propelTime;
        } else if (isEmpowered(stack) && useEnergy(stack, true, !shouldExtract)) {
            propel(entity);
        }

        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (!world.isClientSide() && entity instanceof LivingEntity && !((LivingEntity) entity).isFallFlying()) {
            propelTime = 0;
        }
    }

    //Used to boost a single time, similar to a rocket.
    public boolean boost(ItemStack stack, LivingEntity entity, int time) {

        if (!entity.getItemBySlot(EquipmentSlotType.CHEST).canElytraFly(entity)) {
            return false;
        }
        boolean isPlayer = entity instanceof PlayerEntity;
        boolean isCreative = isPlayer && ((PlayerEntity) entity).abilities.instabuild;
        if (!useEnergy(stack, getEnergyPerUse(true) * time / energyUseInterval, isCreative)) {
            return false;
        }
        if (!entity.isFallFlying() && isPlayer) {
            ((PlayerEntity) entity).startFallFlying();
        }
        propel(entity, propelSpeed);
        propelTime = time;

        return true;
    }

    public boolean boost(ItemStack stack, LivingEntity entity) {

        return boost(stack, entity, boostTime);
    }

    public void propel(LivingEntity entity, double speed) {

        if (entity.isFallFlying()) {
            Vector3d look = entity.getLookAngle();
            Vector3d velocity = entity.getDeltaMovement();
            entity.setDeltaMovement(velocity.add(look.x * speed - velocity.x * 0.5, look.y * speed - velocity.y * 0.5, look.z * speed - velocity.z * 0.5));

            if (entity.level.isClientSide()) {
                entity.level.addParticle(RedstoneParticleData.REDSTONE, entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
            }
        }
    }

    public void propel(LivingEntity entity) {

        propel(entity, propelSpeed);
    }

    public void brake(LivingEntity entity, double rate) {

        if (entity.isFallFlying()) {
            Vector3d velocity = entity.getDeltaMovement();
            double horzBrake = velocity.x() * velocity.x() + velocity.z() * velocity.z() > 0.16 ? rate : 1;
            double vertBrake = velocity.y() * velocity.y() > 0.2 ? rate : 1;
            entity.setDeltaMovement(velocity.multiply(horzBrake, vertBrake, horzBrake));
        }
    }

    public void brake(LivingEntity entity) {

        brake(entity, brakeRate);
    }

}
