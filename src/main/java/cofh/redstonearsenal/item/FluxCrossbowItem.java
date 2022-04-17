package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.item.ILeftClickHandlerItem;
import cofh.lib.item.impl.CrossbowItemCoFH;
import cofh.lib.util.helpers.ArcheryHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.constants.NBTTags.TAG_AMMO;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.GRAY;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

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
    @OnlyIn (Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        if (getLoadedAmmoCount(stack) > 0) {
            tooltip.add(new TranslationTextComponent("info.cofh.crossbow_loaded"));
            for (ItemStack ammo : getAllLoadedAmmo(stack)) {
                tooltip.add((new StringTextComponent("• ")).append(ammo.getHoverName()));
            }
        }
        if (Screen.hasShiftDown() || CoreConfig.alwaysShowDetails) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreConfig.holdShiftForDetails) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(GRAY));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getItemEnchantability(stack) > 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new EnergyContainerItemWrapper(stack, this, getEnergyCapability());
    }

    @Override
    public float getAmmoModelProperty(ItemStack stack, World world, LivingEntity entity) {

        return getLoadedAmmoCount(stack) * 0.334F;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, false) && getLoadedAmmoCount(stack) < maxCharges && (player.abilities.instabuild || !ArcheryHelper.findAmmo(player, stack).isEmpty())) {
            player.startUsingItem(hand);
            return ActionResult.consume(stack);
        }
        return ActionResult.fail(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity living) {

        if (!world.isClientSide && getLoadedAmmoCount(stack) < maxCharges && loadAmmo(living, stack)) {
            setCharged(stack, true);
            world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_END, living instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE, 1.0F, 1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity living, int durationRemaining) {

        if (!world.isClientSide && durationRemaining < 0 && getLoadedAmmoCount(stack) < maxCharges && loadAmmo(living, stack)) {
            setCharged(stack, true);
            world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_END, living instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE, 1.0F, 1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {

        return false;
    }

    @Override
    public void onLeftClick(PlayerEntity player, ItemStack stack) {

        if (isCharged(stack)) {
            if (isEmpowered(stack)) {
                float xRot = player.xRot;
                int count = getLoadedAmmoCount(stack);
                for (int i = (1 - count) / 2; i <= count / 2; ++i) {
                    player.xRot = xRot - 10 * i;
                    shootLoadedAmmo(player.level, player, Hand.MAIN_HAND, stack);
                }
                player.xRot = xRot;
                setCharged(stack, false);
            } else if (shootLoadedAmmo(player.level, player, Hand.MAIN_HAND, stack) && getLoadedAmmoCount(stack) <= 0) {
                setCharged(stack, false);
            }
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, PlayerEntity player) {

        return player.isCreative();
    }

    @Override
    public void onCrossbowShot(PlayerEntity shooter, Hand hand, ItemStack crossbow, int damage) {

        useEnergy(crossbow, Math.min(getEnergyPerUse(false) * damage, getEnergyStored(crossbow)), shooter.abilities.instabuild);

        if (shooter instanceof ServerPlayerEntity) {
            if (!shooter.level.isClientSide()) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger((ServerPlayerEntity) shooter, crossbow);
            }
            shooter.awardStat(Stats.ITEM_USED.get(crossbow.getItem()));
        }
    }

    // region LOADING
    public ListNBT getLoadedAmmoNBT(ItemStack crossbow) {

        CompoundNBT tag = crossbow.getOrCreateTag();
        if (tag.contains(TAG_AMMO) && tag.getTagType(TAG_AMMO) == 9) {
            return tag.getList(TAG_AMMO, TAG_COMPOUND);
        }
        return new ListNBT();
    }

    public ItemStack[] getAllLoadedAmmo(ItemStack crossbow) {

        return getLoadedAmmoNBT(crossbow).stream().map(nbt -> nbt instanceof CompoundNBT ? ItemStack.of((CompoundNBT) nbt) : ItemStack.EMPTY).toArray(ItemStack[]::new);
    }

    public int getLoadedAmmoCount(ItemStack crossbow) {

        return getLoadedAmmoNBT(crossbow).size();
    }

    @Override
    public boolean loadAmmo(PlayerEntity player, ItemStack crossbow, ItemStack ammo) {

        ListNBT list = getLoadedAmmoNBT(crossbow);
        list.add(ammo.save(new CompoundNBT()));
        crossbow.getOrCreateTag().put(TAG_AMMO, list);
        setCharged(crossbow, true);
        return true;
    }

    @Override
    public ItemStack getLoadedAmmo(ItemStack crossbow) {

        ListNBT list = getLoadedAmmoNBT(crossbow);
        return list.isEmpty() ? ItemStack.EMPTY : ItemStack.of(list.getCompound(0));
    }

    @Override
    public void removeLoadedAmmo(ItemStack crossbow) {

        ListNBT list = getLoadedAmmoNBT(crossbow);
        if (!list.isEmpty()) {
            list.remove(0);
        }
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

    @Override
    public void onModeChange(PlayerEntity player, ItemStack stack) {

        if (isEmpowered(stack)) {
            player.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.4F, 1.0F);
        } else {
            player.level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.6F);
        }
    }

}
