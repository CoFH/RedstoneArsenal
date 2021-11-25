package cofh.redstonearsenal.item;

import cofh.core.util.ProxyUtils;
import cofh.lib.capability.IShieldItem;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.item.impl.ShieldItemCoFH;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static cofh.lib.capability.CapabilityShieldItem.SHIELD_ITEM_CAPABILITY;

public class FluxShieldItem extends ShieldItemCoFH implements IFluxItem {

    public static final float RANGE = 4;
    public static final float REPEL_STRENGTH = 2;

    protected int maxEnergy;
    protected int extract;
    protected int receive;

    public FluxShieldItem(int enchantability, Properties builder, int maxEnergy, int maxTransfer) {

        super(builder);

        this.maxEnergy = maxEnergy;
        this.extract = maxTransfer;
        this.receive = maxTransfer;
        setEnchantability(enchantability);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("blocking"), (stack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem().equals(stack) ? 1.0F : 0.0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new FluxShieldItemWrapper(stack, this);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (isEmpowered(stack)) {
            repel(world, player, stack);
        }
        if (hasEnergy(stack, true)) {
            return super.use(world, player, hand);
        }
        return ActionResult.fail(stack);
    }

    public void repel(World world, LivingEntity living, ItemStack stack) {

        if (useEnergy(stack, true, living instanceof PlayerEntity && ((PlayerEntity) living).abilities.instabuild)) {
            double r2 = RANGE * RANGE;
            AxisAlignedBB searchArea = living.getBoundingBox().inflate(RANGE);
            for (Entity entity : world.getEntities(living, searchArea, EntityPredicates.NO_CREATIVE_OR_SPECTATOR)) {
                if (living.distanceToSqr(entity) < r2) {
                    if (entity.getDeltaMovement().lengthSqr() < REPEL_STRENGTH * REPEL_STRENGTH) {
                        entity.setDeltaMovement(entity.position().subtract(living.position()).normalize().scale(REPEL_STRENGTH));
                    } else {
                        entity.setDeltaMovement(entity.getDeltaMovement().reverse());
                    }
                }
            }
        }
    }

    @Override
    public void onUseTick(World world, LivingEntity living, ItemStack stack, int useDuration) {

        if (!hasEnergy(stack, true)) {
            living.releaseUsingItem();
        }
    }

    @Override
    public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {

        return stack.getItem() instanceof FluxShieldItem; // && !(entity != null && entity.getUseItem().equals(stack) && !hasEnergy(stack, true));
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairMaterial) {

        return false;
    }

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

            super(shieldItem, container);
            this.shieldItem = shieldItem;
        }

        @Override
        public void onBlock(LivingEntity entity, DamageSource source, float amount) {

            if (isEmpowered(shieldItem)) {
                repel(entity.level, entity, shieldItem);
            }
            if (amount >= 3.0F && !(entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.instabuild)) {
                int energy = Math.min(getEnergyStored(), MathHelper.ceil(amount) * getEnergyPerUse(false));
                int extract = getExtract(shieldItem);
                for (; energy > 0; energy -= extract) {
                    useEnergy(shieldItem, Math.min(extract, energy), false);
                }
            }
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
