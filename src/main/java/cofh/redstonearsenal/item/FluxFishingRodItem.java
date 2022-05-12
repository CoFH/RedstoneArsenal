package cofh.redstonearsenal.item;

import cofh.core.config.CoreClientConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.item.impl.FishingRodItemCoFH;
import cofh.lib.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public class FluxFishingRodItem extends FishingRodItemCoFH implements IMultiModeFluxItem {

    protected double reelSpeed = 0.2;
    protected int reelEnergyUseInterval = 8;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxFishingRodItem(int enchantability, int luckModifier, int speedModifier, Properties builder, int energy, int xfer) {

        super(builder);

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;
        setParams(enchantability, luckModifier, speedModifier);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("cast"), (stack, world, entity, seed) -> entity instanceof Player && ((Player) entity).fishing != null ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), this::getChargedModelProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("empowered"), this::getEmpoweredModelProperty);
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        if (Screen.hasShiftDown() || CoreClientConfig.alwaysShowDetails) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreClientConfig.holdShiftForDetails) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(ChatFormatting.GRAY));
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
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (player.fishing != null) {
            if (isEmpowered(stack) && player.fishing.getHookedIn() != null) {
                if (player.isShiftKeyDown()) {
                    player.fishing.discard();
                } else {
                    player.startUsingItem(hand);
                }
            } else {
                player.fishing.retrieve(stack);
                useEnergy(stack, false, player.abilities.instabuild);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            }
        } else if (hasEnergy(stack, false)) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            if (!world.isClientSide) {
                int luck = EnchantmentHelper.getFishingLuckBonus(stack) + luckModifier;
                int speed = EnchantmentHelper.getFishingSpeedBonus(stack) + speedModifier;
                world.addFreshEntity(new FishingHook(player, world, luck, speed));
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        } else {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public void onUseTick(Level world, LivingEntity living, ItemStack stack, int useDuration) {

        if (living instanceof Player player) {
            if (player.fishing != null && player.fishing.getHookedIn() != null && isEmpowered(stack)) {
                if (living.isShiftKeyDown()) {
                    player.fishing.discard();
                } else if (useEnergy(stack, true, useDuration % reelEnergyUseInterval != 0 || player.abilities.instabuild)) {
                    reelIn(stack, player.fishing);
                    return;
                }
            }
        }
        living.releaseUsingItem();
    }

    public void reelIn(ItemStack stack, FishingHook bobber) {

        Entity owner = bobber.getOwner();
        if (!bobber.level.isClientSide && owner != null) {
            if (bobber.getHookedIn() != null) {
                Vec3 relPos = owner.position().add(owner.getLookAngle()).subtract(bobber.position()).normalize().scale(reelSpeed);
                bobber.getHookedIn().push(relPos.x(), relPos.y(), relPos.z());
                if (relPos.y() > 0) {
                    bobber.getHookedIn().fallDistance = 0;
                }
                return;
            }
            bobber.discard();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity living, int useDuration) {

    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {

        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack p_77626_1_) {

        return 72000;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (attacker instanceof Player player) {
            useEnergy(stack, false, player.abilities.instabuild);
            if (isEmpowered(stack) && player.fishing != null) {
                player.fishing.discard();
            }
        }
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (Utils.isServerWorld(worldIn) && state.getDestroySpeed(worldIn, pos) != 0.0F) {
            useEnergy(stack, false, entityLiving);
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
