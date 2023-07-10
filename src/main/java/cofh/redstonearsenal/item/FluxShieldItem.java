package cofh.redstonearsenal.item;

import cofh.core.config.CoreClientConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.api.capability.IShieldItem;
import cofh.lib.api.item.IEnergyContainerItem;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.item.ShieldItemCoFH;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.capability.CapabilityShieldItem.SHIELD_ITEM_CAPABILITY;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public class FluxShieldItem extends ShieldItemCoFH implements IMultiModeFluxItem {

    protected float repelRange = 4;
    protected float repelStrength = 1.5F;

    protected int maxEnergy;
    protected int extract;
    protected int receive;

    public FluxShieldItem(int enchantability, Properties builder, int maxEnergy, int maxTransfer) {

        super(builder);

        this.maxEnergy = maxEnergy;
        this.extract = maxTransfer;
        this.receive = maxTransfer;
        setEnchantability(enchantability);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("blocking"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem().equals(stack) ? 1.0F : 0.0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), this::getChargedModelProperty);
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

        return new FluxShieldItemWrapper(stack, this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (isEmpowered(stack)) {
            repel(world, player, stack);
        }
        if (hasEnergy(stack, true)) {
            return super.use(world, player, hand);
        }
        return InteractionResultHolder.fail(stack);
    }

    public void repel(Level world, LivingEntity living, ItemStack stack) {

        if (useEnergy(stack, true, living)) {
            float range = getRepelRange(stack);
            double r2 = range * range;
            float strength = getRepelStrength(stack);
            AABB searchArea = living.getBoundingBox().inflate(range);
            for (Entity entity : world.getEntities(living, searchArea, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                if (living.distanceToSqr(entity) < r2) {
                    Vec3 knockback = entity.position().subtract(living.position());
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).knockback(strength, -knockback.x(), -knockback.z());
                    } else {
                        entity.setDeltaMovement(knockback.normalize().scale(strength));
                        entity.hasImpulse = true;
                    }
                }
            }
        }
    }

    public float getRepelRange(ItemStack stack) {

        return repelRange;
    }

    public float getRepelStrength(ItemStack stack) {

        return repelStrength;
    }

    @Override
    public void onUseTick(Level world, LivingEntity living, ItemStack stack, int useDuration) {

        if (!hasEnergy(stack, true)) {
            living.releaseUsingItem();
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {

        return hasEnergy(stack, false) && super.canPerformAction(stack, action);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairMaterial) {

        return false;
    }

    // region DURABILITY BAR
    @Override
    public boolean isBarVisible(ItemStack stack) {

        return IMultiModeFluxItem.super.isBarVisible(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {

        return IMultiModeFluxItem.super.getBarColor(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {

        return IMultiModeFluxItem.super.getBarWidth(stack);
    }
    // endregion

    // region IEnergyContainerItem
    @Override
    public int getExtract(ItemStack container) {

        return extract;
    }

    @Override
    public int getReceive(ItemStack container) {

        return receive;
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {

        return getMaxStored(container, maxEnergy);
    }
    // endregion

    // region CAPABILITY WRAPPER
    protected class FluxShieldItemWrapper extends EnergyContainerItemWrapper implements IShieldItem {

        private final LazyOptional<IShieldItem> holder = LazyOptional.of(() -> this);

        final ItemStack shieldItem;

        FluxShieldItemWrapper(ItemStack shieldItem, IEnergyContainerItem container) {

            super(shieldItem, container, container.getEnergyCapability());
            this.shieldItem = shieldItem;
        }

        @Override
        public boolean canBlock(LivingEntity target, DamageSource source) {

            if (IShieldItem.super.canBlock(target, source)) {
                return true;
            }
            if (!target.isBlocking() || target.isInvulnerableTo(source) || (target.hasEffect(MobEffects.FIRE_RESISTANCE) && source.isFire())) {
                return false;
            }
            //TODO 1.20 change to tags
            return source.getMsgId().equals("flux") && IShieldItem.canBlockDamagePosition(target, source.getSourcePosition());
        }

        @Override
        public float onBlock(LivingEntity target, DamageSource source, float amount) {

            if (isEmpowered(shieldItem)) {
                repel(target.level, target, shieldItem);
            }
            if (amount >= 3.0F && !(target instanceof Player player && player.isCreative())) {
                int energy = Math.min(getEnergyStored(), Mth.ceil(amount) * getEnergyPerUse(false));
                int extract = getExtract(shieldItem);
                for (; energy > 0; energy -= extract) {
                    useEnergy(shieldItem, Math.min(extract, energy), false);
                }
            }
            return amount;
        }

        // region ICapabilityProvider
        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

            if (cap == SHIELD_ITEM_CAPABILITY) {
                return SHIELD_ITEM_CAPABILITY.orEmpty(cap, holder);
            }
            return super.getCapability(cap, side);
        }
        // endregion
    }
    // endregion
}
