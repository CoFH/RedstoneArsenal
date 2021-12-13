package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.item.impl.CrossbowItemCoFH;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.ArcheryHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.GRAY;

public class FluxCrossbowItem extends CrossbowItemCoFH implements IMultiModeFluxItem {

    protected int repeatStartDelay = 20;
    protected int maxRepeats;
    protected int maxCooldown = 200;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    protected int repeats = 0;
    protected int cooldown = 0;

    public FluxCrossbowItem(int enchantability, float accuracyModifier, float damageModifier, float velocityModifier, int maxRepeats, Item.Properties builder, int energy, int xfer) {

        super(enchantability, accuracyModifier, damageModifier, velocityModifier, builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;
        this.maxRepeats = maxRepeats;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), this::getChargedModelProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), this::getEmpoweredModelProperty);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        if (!getLoadedAmmo(stack).isEmpty()) {
            tooltip.add((new TranslationTextComponent("info.cofh.crossbow_loaded")).append(" ").append(getLoadedAmmo(stack).getDisplayName()));
        }
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

        return new EnergyContainerItemWrapper(stack, this, getEnergyCapability());
    }

    public float getPullModelProperty(ItemStack stack, World world, LivingEntity entity) {

        if (entity == null || !entity.getUseItem().equals(stack)) {
            return 0.0F;
        }
        int totalDuration = getUseDuration(stack);
        int duration = totalDuration - entity.getUseItemRemainingTicks();

        if (isEmpowered(stack)) {
            if (repeats >= maxRepeats) {
                return 0.0F;
            }
            totalDuration = getRepeatInterval(stack);
            return MathHelper.clamp((float) (duration - getRepeatStartDelay(stack) + totalDuration * repeats) / totalDuration, 0.0F, 1.0F);
        } else {
            return MathHelper.clamp((float) (duration) / totalDuration, 0.0F, 1.0F);
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, false)) {
            if (!isEmpowered(stack) && isCharged(stack)) {
                setCharged(stack, shootLoadedAmmo(world, player, hand, stack));
            } else if (!ArcheryHelper.findAmmo(player, stack).isEmpty() || player.abilities.instabuild) {
                repeats = 0;
                player.startUsingItem(hand);
            }
            return ActionResult.consume(stack);
        }
        return ActionResult.fail(stack);
    }

    @Override
    public void onUseTick(World world, LivingEntity living, ItemStack stack, int durationRemaining) {

        if (!world.isClientSide() && hasEnergy(stack, false)) {
            int totalDuration = getUseDuration(stack);
            int duration = totalDuration - durationRemaining;

            if (isEmpowered(stack)) {
                if (repeats >= maxRepeats) {
                    return;
                }
                cooldown++;
                totalDuration = getRepeatInterval(stack);
                duration -= getRepeatStartDelay(stack) + totalDuration * repeats;
                if (duration == totalDuration - 2) {
                    if (!loadAmmo(living, stack)) {
                        living.releaseUsingItem();
                        return;
                    }
                } else if (duration == totalDuration) {
                    if (useEnergy(stack, true, living)) {
                        shootLoadedAmmo(world, living, living.getUsedItemHand(), stack);
                    } else {
                        living.releaseUsingItem();
                    }
                    if (++repeats >= maxRepeats) {
                        living.releaseUsingItem();
                    }
                    return;
                }
            }

            if (duration == totalDuration / 4) {
                world.playSound(null, living.getX(), living.getY(), living.getZ(), getStartSound(Utils.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack)), SoundCategory.PLAYERS, 0.5F, 1.0F);
            }
            if (duration == totalDuration / 2 + 1) {
                world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundCategory.PLAYERS, 0.5F, 1.0F);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity living, int durationRemaining) {

        if (!world.isClientSide()) {
            if (isEmpowered(stack)) {
                repeats = maxRepeats + 1;
                startCooldown(living, stack, getUseDuration(stack) - durationRemaining);
            }
            else if (durationRemaining < 0 && !isCharged(stack) && loadAmmo(living, stack)) {
                setCharged(stack, true);
                world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_END, living instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE, 1.0F, 1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F);
            }
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack stack, PlayerEntity player) {

        startCooldown(player, stack, getUseDuration(stack) - player.getUseItemRemainingTicks());
        return true;
    }

    public int getRepeatInterval(ItemStack stack) {

        return 14 - 2 * MathHelper.clamp(Utils.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack), 0, 3);
    }

    public int getRepeatStartDelay(ItemStack stack) {

        return repeatStartDelay;
    }

    public void startCooldown(LivingEntity entity, ItemStack stack, int amount) {

        if (!entity.level.isClientSide() && isEmpowered(stack) && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (amount >= getRepeatStartDelay(stack) + getRepeatInterval(stack)) {
                player.getCooldowns().addCooldown(this, Math.min(amount, maxCooldown));
                cooldown = 0;
            }
        }
    }

    public void startCooldown(LivingEntity entity, ItemStack stack) {

        startCooldown(entity, stack, cooldown);
    }

    @Override
    public void onCrossbowShot(PlayerEntity shooter, Hand hand, ItemStack crossbow, int damage) {

        useEnergy(crossbow, Math.min(getEnergyPerUse(true) * damage, getEnergyStored(crossbow)), shooter.abilities.instabuild);

        if (shooter instanceof ServerPlayerEntity) {
            if (!shooter.level.isClientSide()) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger((ServerPlayerEntity) shooter, crossbow);
            }
            shooter.awardStat(Stats.ITEM_USED.get(crossbow.getItem()));
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

    @Override
    public void onModeChange(PlayerEntity player, ItemStack stack) {

        if (player.getUseItemRemainingTicks() < 0) {
            setMode(stack, isEmpowered(stack) ? 0 : 1);
            player.releaseUsingItem();
            setMode(stack, isEmpowered(stack) ? 0 : 1);
        }
        if (isEmpowered(stack)) {
            player.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.4F, 1.0F);
            setCharged(stack, true);
        } else {
            player.level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.6F);
            setCharged(stack, !getLoadedAmmo(stack).isEmpty());
        }
    }

}
