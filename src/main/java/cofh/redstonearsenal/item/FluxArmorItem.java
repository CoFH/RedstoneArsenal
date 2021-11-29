package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.item.ArmorItemCoFH;
import cofh.core.util.ProxyUtils;
import cofh.lib.util.Utils;
import cofh.redstonearsenal.capability.FluxShieldedEnergyItemWrapper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.GRAY;

public class FluxArmorItem extends ArmorItemCoFH implements IFluxItem {

    protected int maxEnergy;
    protected int extract;
    protected int receive;

    public FluxArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties builder, int maxEnergy, int maxTransfer) {

        super(material, slot, builder);

        this.maxEnergy = maxEnergy;
        this.extract = maxTransfer;
        this.receive = maxTransfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F);
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
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {

        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getItemEnchantability(stack) > 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new FluxShieldedEnergyItemWrapper(stack, getEnergyPerUse(true)); //TODO: shield when empowered
    }

    @Override
    public boolean isDamageable(ItemStack stack) {

        return hasEnergy(stack, false);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {

        useEnergy(stack, Math.min(getEnergyStored(stack), amount * getEnergyPerUse(false)), entity);
        return -1;
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

}
