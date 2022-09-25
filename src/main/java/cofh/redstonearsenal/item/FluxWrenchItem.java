package cofh.redstonearsenal.item;

import cofh.core.config.CoreClientConfig;
import cofh.core.item.ItemCoFH;
import cofh.core.util.ProxyUtils;
import cofh.lib.api.block.IDismantleable;
import cofh.lib.api.block.IWrenchable;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.BlockHelper;
import cofh.redstonearsenal.entity.ThrownFluxWrench;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static cofh.lib.util.references.CoreReferences.WRENCHED;

public class FluxWrenchItem extends ItemCoFH implements IMultiModeFluxItem {

    protected static final Set<Enchantment> VALID_ENCHANTS = new ObjectOpenHashSet<>();
    protected final float damage;
    protected final float attackSpeed;
    protected final int throwCooldown;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxWrenchItem(Tier tier, float attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

        super(builder);

        this.damage = tier.getAttackDamageBonus() + attackDamageIn;
        this.attackSpeed = attackSpeedIn;
        this.throwCooldown = (int) (20 / (4.0F + attackSpeedIn)) + 2;
        setEnchantability(tier.getEnchantmentValue());

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

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
    public void tooltipDelegate(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        IMultiModeFluxItem.super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    public static void initEnchants() {

        VALID_ENCHANTS.add(Enchantments.SHARPNESS);
        VALID_ENCHANTS.add(Enchantments.FIRE_ASPECT);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {

        return super.canApplyAtEnchantingTable(stack, enchantment) || VALID_ENCHANTS.contains(enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getItemEnchantability(stack) > 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, false)) {
            if (!world.isClientSide()) {
                world.addFreshEntity(new ThrownFluxWrench(world, player, stack));
                player.inventory.removeItem(stack);
                player.getCooldowns().addCooldown(this, getRangedAttackCooldown(stack));
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    protected boolean useDelegate(ItemStack stack, UseOnContext context) {

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null || world.isEmptyBlock(pos)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (player.isSecondaryUseActive() && block instanceof IDismantleable dismantleable && dismantleable.canDismantle(world, pos, state, player)) {
            if (Utils.isServerWorld(world)) {
                BlockHitResult target = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
                dismantleable.dismantleBlock(world, pos, state, target, player, false);
            }
            player.swing(context.getHand());
            return true;
        } else if (!player.isSecondaryUseActive()) {
            if (block instanceof IWrenchable wrenchable && wrenchable.canWrench(world, pos, state, player)) {
                BlockHitResult target = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
                wrenchable.wrenchBlock(world, pos, state, target, player);
                return true;
            }
            return BlockHelper.attemptRotateBlock(state, world, pos);
        }
        return false;
    }

    public boolean useRanged(Level world, ItemStack stack, Player player, BlockHitResult result) {

        if (result.getType() == HitResult.Type.MISS) {
            return false;
        }
        BlockPos pos = result.getBlockPos();
        if (player == null || world.isEmptyBlock(pos) || !(hasEnergy(stack, false) || player.abilities.instabuild) || !player.mayUseItemAt(pos, result.getDirection(), stack)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IWrenchable wrenchable && wrenchable.canWrench(world, pos, state, player)) {
            wrenchable.wrenchBlock(world, pos, state, result, player);
            useEnergy(stack, false, player.abilities.instabuild);
            return true;
        } else if (BlockHelper.attemptRotateBlock(state, world, pos)) {
            useEnergy(stack, false, player.abilities.instabuild);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack) && useDelegate(stack, context) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        target.addEffect(new MobEffectInstance(WRENCHED, 60, 0, false, false));
        useEnergy(stack, false, attacker);
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (Utils.isServerWorld(worldIn) && state.getDestroySpeed(worldIn, pos) != 0.0F) {
            useEnergy(stack, false, entityLiving);
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {

        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", getAttackSpeed(stack), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    protected float getAttackDamage(ItemStack stack) {

        return hasEnergy(stack, false) ? damage : 0.0F;
    }

    public float getRangedAttackDamage(ItemStack stack) {

        return getAttackDamage(stack);
    }

    protected float getAttackSpeed(ItemStack stack) {

        return attackSpeed;
    }

    protected int getRangedAttackCooldown(ItemStack stack) {

        return throwCooldown;
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
}
