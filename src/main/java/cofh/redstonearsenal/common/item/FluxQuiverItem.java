package cofh.redstonearsenal.common.item;

import cofh.core.common.config.CoreClientConfig;
import cofh.core.common.item.ItemCoFH;
import cofh.core.util.ProxyUtils;
import cofh.lib.api.capability.IArcheryAmmoItem;
import cofh.lib.common.energy.EnergyContainerItemWrapper;
import cofh.redstonearsenal.common.entity.FluxArrow;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

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

        return getEnchantmentValue(stack) > 0;
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
    public static class AmmoWrapper extends EnergyContainerItemWrapper implements IArcheryAmmoItem {

        final FluxQuiverItem quiverItem;

        public AmmoWrapper(ItemStack containerIn, FluxQuiverItem itemIn) {

            super(containerIn, itemIn);
            this.quiverItem = itemIn;
        }

        @Override
        public void onArrowLoosed(Player shooter) {

            quiverItem.useEnergy(container, quiverItem.isEmpowered(container), shooter != null && shooter.abilities.instabuild);
        }

        @Override
        public AbstractArrow createArrowEntity(Level world, Player shooter) {

            FluxArrow arrow = new FluxArrow(world, shooter);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            if (quiverItem.isEmpowered(container)) {
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

            return !quiverItem.hasEnergy(container, quiverItem.isEmpowered(container));
        }

        @Override
        public boolean isInfinite(ItemStack bow, Player shooter) {

            return shooter != null && shooter.abilities.instabuild || getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
        }

    }
    // endregion
}
