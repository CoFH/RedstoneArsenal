package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.item.ItemCoFH;
import cofh.core.util.ProxyUtils;
import cofh.lib.block.IDismantleable;
import cofh.lib.block.IWrenchable;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.BlockHelper;
import cofh.redstonearsenal.entity.FluxWrenchEntity;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.potion.EffectInstance;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static cofh.lib.util.references.CoreReferences.WRENCHED;
import static net.minecraft.util.text.TextFormatting.GRAY;

public class FluxWrenchItem extends ItemCoFH implements IFluxItem {

    protected static final Set<Enchantment> VALID_ENCHANTS = new ObjectOpenHashSet<>();
    protected final float damage;
    protected final float attackSpeed;
    protected final int throwCooldown;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxWrenchItem(IItemTier tier, float attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

        super(builder);

        this.damage = tier.getAttackDamageBonus() + attackDamageIn;
        this.attackSpeed = attackSpeedIn;
        this.throwCooldown = (int) (20 / attackSpeedIn) + 2;
        setEnchantability(tier.getEnchantmentValue());

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

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
    public void tooltipDelegate(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        //TODO: wrench stuff
        IFluxItem.super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
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
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (hasEnergy(stack, isEmpowered(stack))) {
            if (!world.isClientSide()) {
                world.addFreshEntity(new FluxWrenchEntity(world, player, stack));
                player.inventory.removeItem(stack);
                player.getCooldowns().addCooldown(this, getRangedAttackCooldown(stack));
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return ActionResult.success(stack);
        }
        return ActionResult.fail(stack);
    }

    protected boolean useDelegate(ItemStack stack, ItemUseContext context) {

        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        PlayerEntity player = context.getPlayer();

        if (player == null || world.isEmptyBlock(pos)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (player.isSecondaryUseActive() && block instanceof IDismantleable && ((IDismantleable) block).canDismantle(world, pos, state, player)) {
            if (Utils.isServerWorld(world)) {
                BlockRayTraceResult target = new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
                ((IDismantleable) block).dismantleBlock(world, pos, state, target, player, false);
            }
            player.swing(context.getHand());
            return true;
        } else if (!player.isSecondaryUseActive()) {
            if (block instanceof IWrenchable && ((IWrenchable) block).canWrench(world, pos, state, player)) {
                BlockRayTraceResult target = new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
                ((IWrenchable) block).wrenchBlock(world, pos, state, target, player);
                return true;
            }
            return BlockHelper.attemptRotateBlock(state, world, pos);
        }
        return false;
    }

    public boolean useRanged(World world, ItemStack stack, PlayerEntity player, BlockRayTraceResult result) {

        if (result.getType() == RayTraceResult.Type.MISS) {
            return false;
        }
        BlockPos pos = result.getBlockPos();
        if (player == null || world.isEmptyBlock(pos) || !(hasEnergy(stack, false) || player.abilities.instabuild) || !player.mayUseItemAt(pos, result.getDirection(), stack)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        //        if (player.isSecondaryUseActive() && block instanceof IDismantleable && ((IDismantleable) block).canDismantle(world, pos, state, player)) {
        //            if (Utils.isServerWorld(world)) {
        //                BlockRayTraceResult target = new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
        //                ((IDismantleable) block).dismantleBlock(world, pos, state, target, player, false);
        //            }
        //            player.swing(context.getHand());
        //            return true;
        //        } else if (!player.isSecondaryUseActive()) {
        if (block instanceof IWrenchable && ((IWrenchable) block).canWrench(world, pos, state, player)) {
            ((IWrenchable) block).wrenchBlock(world, pos, state, result, player);
            useEnergy(stack, false, player.abilities.instabuild);
            return true;
        } else if (BlockHelper.attemptRotateBlock(state, world, pos)) {
            useEnergy(stack, false, player.abilities.instabuild);
            return true;
        }
        //        }
        return false;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {

        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.FAIL;
        }
        return player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), context.getItemInHand()) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {

        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        return player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack) && useDelegate(stack, context) ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        target.addEffect(new EffectInstance(WRENCHED, 60, 0, false, false));
        useEnergy(stack, false, attacker);
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (Utils.isServerWorld(worldIn) && state.getDestroySpeed(worldIn, pos) != 0.0F) {
            useEnergy(stack, false, entityLiving);
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {

        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlotType.MAINHAND) {
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
