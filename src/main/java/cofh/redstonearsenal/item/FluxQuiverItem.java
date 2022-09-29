package cofh.redstonearsenal.item;

import cofh.core.config.CoreClientConfig;
import cofh.core.item.ItemCoFH;
import cofh.core.util.ProxyUtils;
import cofh.lib.api.capability.IArcheryAmmoItem;
import cofh.lib.api.item.IEnergyContainerItem;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.redstonearsenal.entity.FluxArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.capability.CapabilityArchery.AMMO_ITEM_CAPABILITY;
import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public class FluxQuiverItem extends ItemCoFH implements IMultiModeFluxItem {

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;
    protected int energyPerUse = ENERGY_PER_USE;
    protected int energyPerUseEmpowered = ENERGY_PER_USE_EMPOWERED;

    public FluxQuiverItem(int enchantability, Properties builder, int energy, int xfer) {

        this(enchantability, builder, energy, xfer, 1.0F);
    }

    public FluxQuiverItem(int enchantability, Properties builder, int energy, int xfer, float energyUseMod) {

        super(builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;
        this.energyPerUse *= energyUseMod;
        this.energyPerUseEmpowered *= energyUseMod;
        setEnchantability(enchantability);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), this::getChargedModelProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("empowered"), this::getEmpoweredModelProperty);
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        if (Screen.hasShiftDown() || CoreClientConfig.alwaysShowDetails.get()) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreClientConfig.holdShiftForDetails.get()) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void tooltipDelegate(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        IMultiModeFluxItem.super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getItemEnchantability(stack) > 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {

        return new FluxQuiverItemWrapper(stack, this);
    }

    @Override
    public int getEnergyPerUse(boolean empowered) {

        return empowered ? energyPerUseEmpowered : energyPerUse;
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
    protected class FluxQuiverItemWrapper extends EnergyContainerItemWrapper implements IArcheryAmmoItem {

        private final LazyOptional<IArcheryAmmoItem> holder = LazyOptional.of(() -> this);

        final ItemStack quiverItem;

        FluxQuiverItemWrapper(ItemStack quiverItemContainer, IEnergyContainerItem item) {

            super(quiverItemContainer, item, item.getEnergyCapability());
            this.quiverItem = quiverItemContainer;
        }

        @Override
        public void onArrowLoosed(Player shooter) {

            useEnergy(quiverItem, isEmpowered(quiverItem), shooter != null && shooter.abilities.instabuild);
        }

        @Override
        public AbstractArrow createArrowEntity(Level world, Player shooter) {

            FluxArrow arrow = new FluxArrow(world, shooter);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            if (isEmpowered(quiverItem)) {
                ItemStack weapon = shooter.getMainHandItem().isEmpty() ? shooter.getOffhandItem() : shooter.getMainHandItem();
                if (!weapon.isEmpty()) {
                    if (weapon.getItem() instanceof CrossbowItem) {
                        arrow.setExplodeArrow(true);
                        arrow.setBaseDamage(8);
                    } else {
                        arrow.setNoGravity(true);
                    }
                }
            }
            return arrow;
        }

        @Override
        public boolean isEmpty(Player shooter) {

            return !hasEnergy(quiverItem, isEmpowered(quiverItem));
        }

        @Override
        public boolean isInfinite(ItemStack bow, Player shooter) {

            return shooter != null && shooter.abilities.instabuild || getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
        }

        // region ICapabilityProvider
        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

            if (cap == AMMO_ITEM_CAPABILITY) {
                return AMMO_ITEM_CAPABILITY.orEmpty(cap, holder);
            }
            return super.getCapability(cap, side);
        }
        // endregion
    }
    // endregion
}
