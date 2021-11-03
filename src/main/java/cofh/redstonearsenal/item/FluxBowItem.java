package cofh.redstonearsenal.item;

import cofh.core.util.ProxyUtils;
import cofh.lib.capability.CapabilityArchery;
import cofh.lib.capability.IArcheryBowItem;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.item.impl.BowItemCoFH;
import cofh.lib.util.helpers.ArcheryHelper;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
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

public class FluxBowItem extends BowItemCoFH implements IFluxItem {

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxBowItem(Item.Properties builder, int energy, int xfer) {

        super(builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

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
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new FluxBowItemWrapper(stack, accuracyModifier, damageModifier, velocityModifier);
    }

    public float getPullProperty(ItemStack stack, World world, LivingEntity entity) {

        int baseDuration = getUseDuration(stack);
        int duration = baseDuration - entity.getUseItemRemainingTicks() + 1;

        if (isEmpowered(stack)) {

            return ((float) duration) / baseDuration;
        }
        else {
            return MathHelper.clamp((float) (duration) / baseDuration, 0.0F, 1.0F);
        }
    }

//    @Override
//    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
//
//        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
//        //TODO?
//        return multimap;
//    }

//    @Override
//    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
//
//        ItemStack stack = player.getItemInHand(hand);
//        //TODO
//        return ActionResult.pass(stack);
//    }
//
//    @Override
//    public void onUseTick(World world, LivingEntity living, ItemStack stack, int useDuration) {
//
//        //TODO
//    }
//
//    @Override
//    public void releaseUsing(ItemStack stack, World world, LivingEntity living, int useDuration) {
//
//        //TODO
//    }

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

            return accuracyModifier;
        }

        @Override
        public float getDamageModifier(PlayerEntity shooter) {

            return damageModifier;
        }

        @Override
        public float getVelocityModifier(PlayerEntity shooter) {

            return velocityModifier;
        }

        @Override
        public void onArrowLoosed(PlayerEntity shooter) {

            bowItem.hurtAndBreak(1, shooter, (entity) -> entity.broadcastBreakEvent(shooter.getUsedItemHand()));
        }

        @Override
        public boolean fireArrow(ItemStack arrow, PlayerEntity shooter, int charge, World world) {

            return ArcheryHelper.fireArrow(bowItem, arrow, shooter, charge, world);
        }

        // region ICapabilityProvider
        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

            if (cap == CapabilityArchery.BOW_ITEM_CAPABILITY) {
                return CapabilityArchery.BOW_ITEM_CAPABILITY.orEmpty(cap, holder);
            }
            return super.getCapability(cap, side);
        }
        // endregion
    }
    // endregion
}
