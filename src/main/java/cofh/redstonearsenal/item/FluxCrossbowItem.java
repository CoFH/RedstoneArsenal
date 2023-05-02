package cofh.redstonearsenal.item;

import cofh.core.config.CoreClientConfig;
import cofh.core.item.ILeftClickHandlerItem;
import cofh.core.util.ProxyUtils;
import cofh.core.util.helpers.ArcheryHelper;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.item.CrossbowItemCoFH;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.constants.NBTTags.TAG_AMMO;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.nbt.Tag.TAG_COMPOUND;

public class FluxCrossbowItem extends CrossbowItemCoFH implements IMultiModeFluxItem, ILeftClickHandlerItem {

    protected final int maxCharges;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxCrossbowItem(int enchantability, float accuracyModifier, float damageModifier, float velocityModifier, int maxCharges, Item.Properties builder, int energy, int xfer) {

        super(enchantability, accuracyModifier, damageModifier, velocityModifier, builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;
        this.maxCharges = maxCharges;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), this::getChargedModelProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("empowered"), this::getEmpoweredModelProperty);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        if (getLoadedAmmoCount(stack) > 0) {
            tooltip.add(Component.translatable("info.cofh.crossbow_loaded"));
            for (ItemStack ammo : getAllLoadedAmmo(stack)) {
                tooltip.add((Component.literal("- ")).append(ammo.getHoverName()));
            }
        }
        if (Screen.hasShiftDown() || CoreClientConfig.alwaysShowDetails.get()) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreClientConfig.holdShiftForDetails.get()) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getEnchantmentValue(stack) > 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {

        return new EnergyContainerItemWrapper(stack, this, getEnergyCapability());
    }

    @Override
    public float getAmmoModelProperty(ItemStack stack, Level world, LivingEntity entity, int seed) {

        return getLoadedAmmoCount(stack) * 0.334F;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, false) && getLoadedAmmoCount(stack) < maxCharges && (player.abilities.instabuild || !ArcheryHelper.findAmmo(player, stack).isEmpty())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity living) {

        if (!world.isClientSide && getLoadedAmmoCount(stack) < maxCharges && loadAmmo(living, stack)) {
            setCharged(stack, true);
            world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_END, living instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 1.0F, 1.0F / (world.random.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity living, int durationRemaining) {

        if (!world.isClientSide && durationRemaining < 0 && getLoadedAmmoCount(stack) < maxCharges && loadAmmo(living, stack)) {
            setCharged(stack, true);
            world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_END, living instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 1.0F, 1.0F / (world.random.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {

        return false;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {

        return player.isCreative();
    }

    @Override
    public void onCrossbowShot(Player shooter, InteractionHand hand, ItemStack crossbow, int damage) {

        useEnergy(crossbow, Math.min(getEnergyPerUse(false) * damage, getEnergyStored(crossbow)), shooter.abilities.instabuild);

        if (shooter instanceof ServerPlayer) {
            if (!shooter.level.isClientSide()) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger((ServerPlayer) shooter, crossbow);
            }
            shooter.awardStat(Stats.ITEM_USED.get(crossbow.getItem()));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {

        return !oldStack.equals(newStack) && (slotChanged || getEnergyStored(oldStack) > 0 != getEnergyStored(newStack) > 0 || getLoadedAmmoCount(oldStack) != getLoadedAmmoCount(newStack));
    }

    // region LOADING
    public ListTag getLoadedAmmoNBT(ItemStack crossbow) {

        CompoundTag tag = crossbow.getOrCreateTag();
        if (tag.contains(TAG_AMMO) && tag.getTagType(TAG_AMMO) == 9) {
            return tag.getList(TAG_AMMO, TAG_COMPOUND);
        }
        return new ListTag();
    }

    public ItemStack[] getAllLoadedAmmo(ItemStack crossbow) {

        return getLoadedAmmoNBT(crossbow).stream().map(nbt -> nbt instanceof CompoundTag ? ItemStack.of((CompoundTag) nbt) : ItemStack.EMPTY).toArray(ItemStack[]::new);
    }

    public int getLoadedAmmoCount(ItemStack crossbow) {

        return getLoadedAmmoNBT(crossbow).size();
    }

    @Override
    public boolean loadAmmo(Player player, ItemStack crossbow, ItemStack ammo) {

        ListTag list = getLoadedAmmoNBT(crossbow);
        list.add(ammo.save(new CompoundTag()));
        crossbow.getOrCreateTag().put(TAG_AMMO, list);
        setCharged(crossbow, true);
        return true;
    }

    @Override
    public ItemStack getLoadedAmmo(ItemStack crossbow) {

        ListTag list = getLoadedAmmoNBT(crossbow);
        return list.isEmpty() ? ItemStack.EMPTY : ItemStack.of(list.getCompound(0));
    }

    @Override
    public void removeLoadedAmmo(ItemStack crossbow) {

        ListTag list = getLoadedAmmoNBT(crossbow);
        if (!list.isEmpty()) {
            list.remove(0);
        }
    }
    // endregion

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

    // region ILeftClickHandlerItem
    @Override
    public void onLeftClick(Player player, ItemStack stack) {

        if (isCharged(stack)) {
            if (isEmpowered(stack)) {
                float xRot = player.xRot;
                int count = getLoadedAmmoCount(stack);
                for (int i = (1 - count) / 2; i <= count / 2; ++i) {
                    player.xRot = xRot - 10 * i;
                    shootLoadedAmmo(player.level, player, InteractionHand.MAIN_HAND, stack);
                }
                player.xRot = xRot;
                setCharged(stack, false);
            } else if (shootLoadedAmmo(player.level, player, InteractionHand.MAIN_HAND, stack) && getLoadedAmmoCount(stack) <= 0) {
                setCharged(stack, false);
            }
        }
    }
    // endregion

}
