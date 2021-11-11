package cofh.redstonearsenal.item;

import cofh.core.util.ProxyUtils;
import cofh.lib.item.impl.PickaxeItemCoFH;
import cofh.lib.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.redstonearsenal.init.RSAReferences.FLUX_GLOW_AIR;
import static net.minecraft.block.Blocks.AIR;

public class FluxPickaxeItem extends PickaxeItemCoFH implements IFluxItem {

    protected final int LOW_LIGHT_THRESHOLD = 5;
    protected final int REMOVE_RADIUS = 10;
    protected final float damage;
    protected final float attackSpeed;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxPickaxeItem(IItemTier tier, int attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

        super(tier, attackDamageIn, attackSpeedIn, builder);

        this.damage = getAttackDamage();
        this.attackSpeed = attackSpeedIn;

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && isEmpowered(stack) ? 1F : 0F);
    }

    protected float getEfficiency(ItemStack stack) {

        return hasEnergy(stack, false) ? speed : 1.0F;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltipDelegate(stack, worldIn, tooltip, flagIn);
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
    public boolean canHarvestBlock(ItemStack stack, BlockState state) {

        return isCorrectToolForDrops(state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {

        return getToolTypes(stack).stream().anyMatch(state::isToolEffective) ? getEfficiency(stack) : 1.0F;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        useEnergy(stack, false, ((PlayerEntity) attacker).abilities.instabuild);
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {

        if (Utils.isServerWorld(worldIn) && state.getDestroySpeed(worldIn, pos) != 0.0F) {
            useEnergy(stack, false, entityLiving instanceof PlayerEntity && ((PlayerEntity) entityLiving).abilities.instabuild);
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

    @Override
    public ActionResultType useOn(ItemUseContext context) {

        ItemStack tool = context.getItemInHand();
        if (context.getPlayer() != null && useEnergy(tool, true, context.getPlayer().abilities.instabuild)) {
            if (context.getPlayer().isCrouching()) {
                int r = REMOVE_RADIUS;
                for (BlockPos pos : BlockPos.betweenClosed(context.getClickedPos().offset(-r, -r, -r), context.getClickedPos().offset(r, r, r))) {
                    if (pos.distSqr(context.getClickedPos()) < r * r && context.getLevel().getBlockState(pos).getBlock().equals(FLUX_GLOW_AIR)) {
                        context.getLevel().setBlockAndUpdate(pos, AIR.defaultBlockState());
                    }
                }
                return ActionResultType.SUCCESS;
            }
            else {
                BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
                if (context.getLevel().isEmptyBlock(pos)) {
                    context.getLevel().setBlockAndUpdate(pos, FLUX_GLOW_AIR.defaultBlockState());
                    return ActionResultType.SUCCESS;
                }

            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {

        super.inventoryTick(stack, world, entity, itemSlot, isSelected);

        if (!world.isClientSide() && isEmpowered(stack) && world.getGameTime() % 8 == 0) {
            BlockPos pos = entity.blockPosition();
            if (world.isEmptyBlock(pos) && world.getRawBrightness(pos, world.getSkyDarken()) <= LOW_LIGHT_THRESHOLD) {
                if (useEnergy(stack, true, entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.instabuild)) {
                    world.setBlockAndUpdate(pos, FLUX_GLOW_AIR.defaultBlockState());
                }
            }
        }
    }

    protected float getAttackDamage(ItemStack stack) {

        return hasEnergy(stack, false) ? damage : 0.0F;
    }

    protected float getAttackSpeed(ItemStack stack) {

        return attackSpeed;
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
