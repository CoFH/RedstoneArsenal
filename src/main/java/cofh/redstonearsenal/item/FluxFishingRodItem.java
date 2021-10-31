package cofh.redstonearsenal.item;

import cofh.core.util.ProxyUtils;
import cofh.lib.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class FluxFishingRodItem extends FishingRodItem implements IFluxItem {

    public static final double REEL_SPEED = 0.2;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxFishingRodItem(Properties builder, int energy, int xfer) {

        super(builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("cast"), (stack, world, entity) -> entity instanceof PlayerEntity && ((PlayerEntity) entity).fishing != null ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && getMode(stack) > 0 ? 1F : 0F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (player.fishing != null) {
            if (getMode(stack) > 0 && player.fishing.getHookedIn() != null) {
                if (player.isCrouching()) {
                    player.fishing.remove();
                }
                else {
                    player.startUsingItem(hand);
                }
            }
            else {
                player.fishing.retrieve(stack);
                useEnergy(stack, false, player.abilities.instabuild);

                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            }
        }
        else if (hasEnergy(stack, false)) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            if (!world.isClientSide) {
                world.addFreshEntity(new FishingBobberEntity(player, world, EnchantmentHelper.getFishingLuckBonus(stack), EnchantmentHelper.getFishingSpeedBonus(stack)));
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        else {
            return ActionResult.pass(stack);
        }

        return ActionResult.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public void onUseTick(World world, LivingEntity living, ItemStack stack, int useDuration) {

        if (living instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) living;
            if (player.fishing != null && player.fishing.getHookedIn() != null && getMode(stack) > 0) {
                if (living.isCrouching()) {
                    player.fishing.remove();
                }
                else if ((useDuration & 8) != 0 || hasEnergy(stack, true) || player.abilities.instabuild) {
                    useEnergy(stack, true, player.abilities.instabuild);
                    reelIn(stack, player.fishing);
                    return;
                }
            }
        }
        living.releaseUsingItem();
    }
    
    public void reelIn(ItemStack stack, FishingBobberEntity bobber) {

        Entity owner = bobber.getOwner();
        if (!bobber.level.isClientSide && owner != null) {
            if (bobber.getHookedIn() != null) {
                Vector3d relPos = owner.position().subtract(bobber.position());
                bobber.getHookedIn().setDeltaMovement(bobber.getHookedIn().getDeltaMovement().add(relPos.normalize().scale(FluxFishingRodItem.REEL_SPEED)));
                return;
            }
            bobber.remove();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity living, int useDuration) {
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {

        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack p_77626_1_) {

        return 72000;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) attacker;
            if (!player.abilities.instabuild && hasEnergy(stack)) {
                useEnergy(stack, false, false);
            }
        }
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (Utils.isServerWorld(worldIn) && state.getDestroySpeed(worldIn, pos) != 0.0F) {
            if (entityLiving instanceof PlayerEntity && !((PlayerEntity) entityLiving).abilities.instabuild) {
                useEnergy(stack, false, false);
            }
        }
        return true;
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
