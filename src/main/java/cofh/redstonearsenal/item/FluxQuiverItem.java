package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.item.ItemCoFH;
import cofh.core.util.ProxyUtils;
import cofh.lib.capability.IArcheryAmmoItem;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.util.helpers.ArcheryHelper;
import cofh.redstonearsenal.entity.FluxArrowEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.CrossbowItem;
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

import static cofh.lib.capability.CapabilityArchery.AMMO_ITEM_CAPABILITY;
import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.GRAY;

public class FluxQuiverItem extends ItemCoFH implements IFluxItem {

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
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), this::getEmpoweredModelProperty);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        if (Screen.hasShiftDown() || CoreConfig.alwaysShowDetails) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreConfig.holdShiftForDetails) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(GRAY));
        }
    }

    @Override
    public void tooltipDelegate(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        IFluxItem.super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
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

        return new FluxQuiverItemWrapper(stack, this);
    }

    @Override
    public int getEnergyPerUse(boolean empowered) {

        return empowered ? energyPerUseEmpowered : energyPerUse;
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
    protected class FluxQuiverItemWrapper extends EnergyContainerItemWrapper implements IArcheryAmmoItem {

        private final LazyOptional<IArcheryAmmoItem> holder = LazyOptional.of(() -> this);

        final ItemStack quiverItem;

        FluxQuiverItemWrapper(ItemStack quiverItemContainer, IEnergyContainerItem item) {

            super(quiverItemContainer, item, item.getEnergyCapability());
            this.quiverItem = quiverItemContainer;
        }

        @Override
        public void onArrowLoosed(PlayerEntity shooter) {

            useEnergy(quiverItem, isEmpowered(quiverItem), shooter != null && shooter.abilities.instabuild);
        }

        @Override
        public AbstractArrowEntity createArrowEntity(World world, PlayerEntity shooter) {

            FluxArrowEntity arrow = new FluxArrowEntity(world, shooter);
            arrow.pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
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
        public boolean isEmpty(PlayerEntity shooter) {

            return !hasEnergy(quiverItem, isEmpowered(quiverItem));
        }

        @Override
        public boolean isInfinite(ItemStack bow, PlayerEntity shooter) {

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
