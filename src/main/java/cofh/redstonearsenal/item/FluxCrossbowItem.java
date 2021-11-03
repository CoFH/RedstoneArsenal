package cofh.redstonearsenal.item;

import cofh.core.util.ProxyUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class FluxCrossbowItem extends CrossbowItem implements IFluxItem {

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxCrossbowItem(Item.Properties builder, int energy, int xfer) {

        super(builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("pull"), this::getPullProperty);
//        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("firework"), (stack, world, entity) -> get ? 1F : 0F); TODO: arrow and firework properties
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    public float getPullProperty(ItemStack stack, World world, LivingEntity entity) {

        int baseDuration = getUseDuration(stack);
        int duration = baseDuration - entity.getUseItemRemainingTicks() + 1;

        if (isEmpowered(stack)) {
            int repeats = getRepeats(duration, baseDuration);
            duration = duration - getCumulativeDuration(repeats, baseDuration);
            baseDuration = getCumulativeDuration(repeats + 1, baseDuration) - getCumulativeDuration(repeats, baseDuration);

            return ((float) duration) / baseDuration;
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
        if (!isEmpowered(stack) && isCharged(stack)) {
            performShooting(world, player, hand, stack, getShootingPower(stack), 1.0F);
            setCharged(stack, false);
        }
        else  {
            player.startUsingItem(hand);
        }
        return ActionResult.consume(stack);
    }

    //These functions determine the rate at which the empowered crossbow repeats. They're inverses of each other.
    public int getRepeats(int duration, int baseDuration) {

        float f = ((float) duration) / baseDuration;
        if (f < 3.6) {
            return (int) (6.5F - 0.5F * MathHelper.sqrt(169 - 40 * f));
        }
        return (int) ((f - 1.6F) * 2);
    }

    public int getCumulativeDuration(int repeats, int baseDuration) {

        if (repeats < 4) {
            return Math.round((-0.1F * repeats + 1.3F) * repeats * baseDuration);
        }
        return Math.round((0.5F * repeats + 1.6F) * baseDuration);
    }

    @Override
    public void onUseTick(World world, LivingEntity living, ItemStack stack, int durationRemaining) {

        if (!world.isClientSide) {
            int baseDuration = getUseDuration(stack);
            int duration = baseDuration - durationRemaining + 1;

            if (isEmpowered(stack)) {
                int repeats = getRepeats(duration, baseDuration);
                duration = duration - getCumulativeDuration(repeats, baseDuration);
                baseDuration = getCumulativeDuration(repeats + 1, baseDuration) - getCumulativeDuration(repeats, baseDuration);

                if (duration == 0) {
                    if (useEnergy(stack, true, living instanceof PlayerEntity && ((PlayerEntity) living).abilities.instabuild)) {
                        tryLoadProjectiles(living, stack);
                        performShooting(world, living, living.getUsedItemHand(), stack, getShootingPower(stack), 1.0F);
                    }
                    else {
                        releaseUsing(stack, world, living, durationRemaining);
                    }
                }
            }
            else {
                duration %= baseDuration;
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

        if (isEmpowered(stack)) {
            player.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.4F, 1.0F);
            setCharged(stack, true);
        } else {
            player.level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.6F);
            setCharged(stack, false);
        }
    }
}
