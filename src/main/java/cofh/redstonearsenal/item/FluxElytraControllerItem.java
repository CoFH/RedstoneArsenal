package cofh.redstonearsenal.item;

import cofh.core.item.IMultiModeItem;
import cofh.core.util.ProxyUtils;
import cofh.lib.api.item.ICoFHItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static cofh.lib.util.Constants.TRUE;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static cofh.redstonearsenal.init.RSASounds.SOUND_EMPOWER;
import static cofh.redstonearsenal.init.RSASounds.SOUND_QUELL;

public class FluxElytraControllerItem extends Item implements ICoFHItem, IMultiModeItem {

    public FluxElytraControllerItem(Item.Properties builder) {

        super(builder);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("empowered"), (stack, world, entity, seed) -> isEmpowered(stack) ? 1.0F : 0.0F);
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        if (isEmpowered(stack)) {
            tooltip.add(getTextComponent("info.redstone_arsenal.mode.1").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(getTextComponent("info.redstone_arsenal.mode.0").withStyle(ChatFormatting.GRAY));
        }
        addModeChangeTooltip(this, stack, worldIn, tooltip, flagIn);
    }

    public boolean isEmpowered(ItemStack stack) {

        return getMode(stack) > 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof FluxElytraItem elytra) {
            elytra.setMode(chest, getMode(stack));
            if (elytra.boost(chest, player)) {
                return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
            }
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void onModeChange(Player player, ItemStack stack) {

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof FluxElytraItem elytra) {
            elytra.setMode(chest, getMode(stack));
            if (isEmpowered(stack)) {
                player.level.playSound(null, player.blockPosition(), SOUND_EMPOWER.get(), SoundSource.PLAYERS, 0.4F, 1.0F);
            } else {
                player.level.playSound(null, player.blockPosition(), SOUND_QUELL.get(), SoundSource.PLAYERS, 0.2F, 0.6F);
            }
        } else {
            setMode(stack, isEmpowered(stack) ? 0 : 1);
        }
    }

    // region DISPLAY
    protected Supplier<CreativeModeTab> displayGroup;
    protected Supplier<Boolean> showInGroups = TRUE;
    protected String modId = "";

    @Override
    public FluxElytraControllerItem setDisplayGroup(Supplier<CreativeModeTab> displayGroup) {

        this.displayGroup = displayGroup;
        return this;
    }

    @Override
    public FluxElytraControllerItem setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public FluxElytraControllerItem setShowInGroups(Supplier<Boolean> showInGroups) {

        this.showInGroups = showInGroups;
        return this;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {

        if (!showInGroups.get() || displayGroup != null && displayGroup.get() != null && displayGroup.get() != group) {
            return;
        }
        super.fillItemCategory(group, items);
    }

    @Override
    public Collection<CreativeModeTab> getCreativeTabs() {

        return displayGroup != null && displayGroup.get() != null ? Collections.singletonList(displayGroup.get()) : super.getCreativeTabs();
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(itemStack) : modId;
    }
    // endregion
}
