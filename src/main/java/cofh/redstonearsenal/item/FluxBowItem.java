package cofh.redstonearsenal.item;

import cofh.core.util.ProxyUtils;
import cofh.lib.capability.IArcheryBowItem;
import cofh.lib.capability.templates.ArcheryBowItemWrapper;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.item.impl.BowItemCoFH;
import cofh.lib.util.helpers.ArcheryHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
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

import static cofh.lib.capability.CapabilityArchery.BOW_ITEM_CAPABILITY;

public class FluxBowItem extends BowItemCoFH implements IFluxItem {

    protected final int EMPOWERED_ENERGY_USE_INTERVAL = 20;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxBowItem(int enchantability, float accuracyModifier, float damageModifier, float velocityModifier, Item.Properties builder, int energy, int xfer) {

        super(builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;
        setParams(enchantability, accuracyModifier, damageModifier, velocityModifier);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("pull"), this::getPullProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {

        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getItemEnchantability(stack) > 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new FluxBowItemWrapper(stack, accuracyModifier, damageModifier, velocityModifier);
    }

    public float getPullProperty(ItemStack stack, World world, LivingEntity entity) {

        if (entity == null || !entity.getUseItem().equals(stack)) {
            return 0.0F;
        }
        return MathHelper.clamp((float) (entity.getTicksUsingItem()) / 20F, 0.0F, 1.0F);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, isEmpowered(stack))) {
            return super.use(world, player, hand);
        }
        return ActionResult.fail(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity living, int durationRemaining) {

        if (living instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) living;
            if (useEnergy(stack, isEmpowered(stack), player.abilities.instabuild)) {
                int duration = getUseDuration(stack) - durationRemaining;
                IArcheryBowItem bowCap = stack.getCapability(BOW_ITEM_CAPABILITY).orElse(new ArcheryBowItemWrapper(stack));
                if (bowCap.fireArrow(living.getProjectile(stack), player, duration, world) && isEmpowered(stack) && !player.abilities.instabuild) {
                    int amount = Math.min(duration * ENERGY_PER_USE_EMPOWERED / EMPOWERED_ENERGY_USE_INTERVAL, getEnergyStored(stack));
                    int maxExtract = getExtract(stack);
                    for (; amount > 0; amount -= maxExtract) {
                        useEnergy(stack, Math.min(maxExtract, amount), false);
                    }
                }
            }
        }
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
    protected class FluxBowItemWrapper extends EnergyContainerItemWrapper implements IArcheryBowItem {

        private final LazyOptional<IArcheryBowItem> holder = LazyOptional.of(() -> this);
        private final float accuracyModifier;
        private final float damageModifier;
        private final float velocityModifier;

        final ItemStack bowItem;

        FluxBowItemWrapper(ItemStack bowItemContainer, float accuracyModifier, float damageModifier, float velocityModifier) {

            super(bowItemContainer, (IEnergyContainerItem) bowItemContainer.getItem());
            this.bowItem = bowItemContainer;

            this.accuracyModifier = MathHelper.clamp(accuracyModifier, 0.1F, 10.0F);
            this.damageModifier = MathHelper.clamp(damageModifier, 0.1F, 10.0F);
            this.velocityModifier = MathHelper.clamp(velocityModifier, 0.1F, 10.0F);
        }

        FluxBowItemWrapper(ItemStack bowItemContainer) {

            this(bowItemContainer, 1.0F, 1.0F, 1.0F);
        }

        @Override
        public float getAccuracyModifier(PlayerEntity shooter) {

            if (isEmpowered(bowItem)) {
                int duration = shooter.getTicksUsingItem();
                if (!shooter.abilities.instabuild) {
                    duration = Math.min(duration, getEnergyStored() * EMPOWERED_ENERGY_USE_INTERVAL / ENERGY_PER_USE_EMPOWERED);
                }
                if (duration > 20) {
                    return Math.min(accuracyModifier / MathHelper.sqrt(duration / 20.0F), 10.0F);
                }
            }
            return accuracyModifier;
        }

        @Override
        public float getDamageModifier(PlayerEntity shooter) {

            return damageModifier;
        }

        @Override
        public float getVelocityModifier(PlayerEntity shooter) {

            if (isEmpowered(bowItem)) {
                int duration = shooter.getTicksUsingItem();
                if (!shooter.abilities.instabuild) {
                    duration = Math.min(duration, getEnergyStored() * EMPOWERED_ENERGY_USE_INTERVAL / ENERGY_PER_USE_EMPOWERED);
                }
                if (duration > 20) {
                    return Math.min(velocityModifier * MathHelper.sqrt(duration / 20.0F), 10.0F);
                }
            }
            return velocityModifier;
        }

        @Override
        public void onArrowLoosed(PlayerEntity shooter) {

        }

        @Override
        public boolean fireArrow(ItemStack arrow, PlayerEntity shooter, int charge, World world) {

            return ArcheryHelper.fireArrow(bowItem, arrow, shooter, charge, world);
        }

        // region ICapabilityProvider
        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

            if (cap == BOW_ITEM_CAPABILITY) {
                return BOW_ITEM_CAPABILITY.orEmpty(cap, holder);
            }
            return super.getCapability(cap, side);
        }
        // endregion
    }
    // endregion
}
