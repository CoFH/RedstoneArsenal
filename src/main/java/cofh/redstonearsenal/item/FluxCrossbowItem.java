package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.util.ProxyUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.*;

public class FluxCrossbowItem extends CrossbowItem implements IFluxItem {

    protected static final float[] REPEAT_DURATIONS = getRepeatDurations(1.4F, 0.6F, -0.2F, 20);

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;
    protected int repeats = 1;

    public FluxCrossbowItem(Item.Properties builder, int energy, int xfer) {

        super(builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("pull"), this::getPullProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("firework"), (stack, world, entity) -> !getChargedProjectiles(stack).isEmpty() && (getChargedProjectiles(stack).get(0).getItem() instanceof FireworkRocketItem) ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("arrow"), (stack, world, entity) -> !getChargedProjectiles(stack).isEmpty() && !(getChargedProjectiles(stack).get(0).getItem() instanceof FireworkRocketItem) ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltipDelegate(stack, worldIn, tooltip, flagIn);
        List<ITextComponent> additionalTooltips = new ArrayList<>();
        super.appendHoverText(stack, worldIn, additionalTooltips, flagIn);

        if (!additionalTooltips.isEmpty()) {
            if (Screen.hasShiftDown() || CoreConfig.alwaysShowDetails) {
                tooltip.addAll(additionalTooltips);
            }
            else if (CoreConfig.holdShiftForDetails) {
                tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(GRAY));
            }
        }
    }

    public float getPullProperty(ItemStack stack, World world, LivingEntity entity) {

        if (entity == null || !entity.getUseItem().equals(stack)) {
            return 0.0F;
        }
        int baseDuration = getUseDuration(stack);
        int duration = baseDuration - entity.getUseItemRemainingTicks();

        if (isEmpowered(stack)) {
            if (repeats >= REPEAT_DURATIONS.length) {
                return 0.0F;
            }
            int next = MathHelper.floor(REPEAT_DURATIONS[repeats] * getUseDuration(stack));
            int prev = MathHelper.floor(REPEAT_DURATIONS[repeats - 1] * getUseDuration(stack));

            return MathHelper.clamp(((float) duration - prev) / (next - prev), 0.0F, 1.0F);
        }
        else {
            return MathHelper.clamp((float) (duration) / baseDuration, 0.0F, 1.0F);
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {

        return hasEnergy(stack, false);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {

        amount = Math.min(getEnergyStored(stack), amount * getEnergyPerUse(false));
        useEnergy(stack, amount, entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.instabuild);
        return -1;
    }

    @Override
    public int getUseDuration(ItemStack stack) {

        return getChargeDuration(stack);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, false)) {
            if (!isEmpowered(stack) && isCharged(stack)) {
                performShooting(world, player, hand, stack, getShootingPower(stack), 1.0F);
                setCharged(stack, false);
            }
            else {
                repeats = 1;
                player.startUsingItem(hand);
            }
            return ActionResult.consume(stack);
        }
        return ActionResult.pass(stack);
    }

    public static float[] getRepeatDurations(float start, float end, float change, int repeats) {

        float[] durations = new float[repeats];
        float duration = 0;
        for (int i = 0; i < repeats; ++i) {
            durations[i] = duration;
            duration += start;
            start = Math.max(start + change, end);
        }
        return durations;
    }

    @Override
    public void onUseTick(World world, LivingEntity living, ItemStack stack, int durationRemaining) {

        if (!world.isClientSide() && hasEnergy(stack, false)) {
            int baseDuration = getUseDuration(stack);
            int duration = baseDuration - durationRemaining;

            if (isEmpowered(stack)) {
                if (repeats >= REPEAT_DURATIONS.length) {
                    return;
                }

                int next = MathHelper.floor(REPEAT_DURATIONS[repeats] * getUseDuration(stack));
                if (repeats > 0 && duration == next) {
                    if (!world.isClientSide()) {
                        ++repeats;
                    }
                    if (useEnergy(stack, true, living instanceof PlayerEntity && ((PlayerEntity) living).abilities.instabuild)) {
                        tryLoadProjectiles(living, stack);
                        performShooting(world, living, living.getUsedItemHand(), stack, getShootingPower(stack), 1.0F);
                    }
                    else {
                        living.releaseUsingItem();
                        return;
                    }
                    if (repeats >= REPEAT_DURATIONS.length) {
                        living.releaseUsingItem();
                    }
                    return;
                }

                int prev = MathHelper.floor(REPEAT_DURATIONS[repeats - 1] * getUseDuration(stack));
                duration -= prev;
                baseDuration = next - prev;
            }

            if (duration == baseDuration / 4) {
                world.playSound(null, living.getX(), living.getY(), living.getZ(), getStartSound(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack)), SoundCategory.PLAYERS, 0.5F, 1.0F);
            }
            if (duration == baseDuration / 2) {
                world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundCategory.PLAYERS, 0.5F, 1.0F);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity living, int durationRemaining) {

        if (!isEmpowered(stack) && durationRemaining < 0 && !isCharged(stack) && tryLoadProjectiles(living, stack)) {
            setCharged(stack, true);
            world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_END, living instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE, 1.0F, 1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
        else if (isEmpowered(stack) && living instanceof PlayerEntity) {
            int cooldown = MathHelper.clamp(getUseDuration(stack) - durationRemaining, 40, 200);
            ((PlayerEntity) living).getCooldowns().addCooldown(this, cooldown);
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
        }
        else {
            player.level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.6F);
            setCharged(stack, !getChargedProjectiles(stack).isEmpty());
        }
    }
}
