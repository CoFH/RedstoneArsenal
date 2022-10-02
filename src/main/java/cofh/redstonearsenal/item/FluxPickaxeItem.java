package cofh.redstonearsenal.item;

import cofh.core.config.CoreClientConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.item.PickaxeItemCoFH;
import cofh.lib.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static cofh.redstonearsenal.init.RSABlocks.FLUX_GLOW_AIR;
import static net.minecraft.world.level.block.Blocks.AIR;

public class FluxPickaxeItem extends PickaxeItemCoFH implements IMultiModeFluxItem {

    protected final int LOW_LIGHT_THRESHOLD = 5;
    protected final int REMOVE_RADIUS = 10;
    protected final float damage;
    protected final float attackSpeed;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxPickaxeItem(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

        super(tier, attackDamageIn, attackSpeedIn, builder);

        this.damage = getAttackDamage();
        this.attackSpeed = attackSpeedIn;

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), this::getChargedModelProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("empowered"), this::getEmpoweredModelProperty);
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

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
    public boolean canPerformAction(ItemStack stack, ToolAction action) {

        return hasEnergy(stack, false) && super.canPerformAction(stack, action);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

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
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {

        return hasEnergy(stack, false) && super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {

        return isCorrectToolForDrops(stack, state) ? speed : 1.0F;
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

    @Override
    public InteractionResult useOn(UseOnContext context) {

        ItemStack tool = context.getItemInHand();
        Player player = context.getPlayer();
        if (player != null) {
            Level world = context.getLevel();
            if (player.isShiftKeyDown()) {
                if (useEnergy(tool, true, player.abilities.instabuild)) {
                    int r = REMOVE_RADIUS;
                    int r2 = r * r;
                    for (BlockPos pos : BlockPos.betweenClosed(context.getClickedPos().offset(-r, -r, -r), context.getClickedPos().offset(r, r, r))) {
                        if (pos.distSqr(context.getClickedPos()) < r2 && world.getBlockState(pos).getBlock().equals(FLUX_GLOW_AIR.get())) {
                            removeAir(world, player, pos, 0.3F);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            } else {
                BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
                BlockState state = world.getBlockState(pos);
                if (state.getBlock().equals(FLUX_GLOW_AIR.get()) && useEnergy(tool, false, player.abilities.instabuild)) {
                    removeAir(world, player, pos, 0.5F);
                    return InteractionResult.SUCCESS;
                } else if (state.isAir() && useEnergy(tool, true, player.abilities.instabuild)) {
                    placeAir(world, player, pos, 0.5F);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {

        super.inventoryTick(stack, world, entity, itemSlot, isSelected);

        if (!world.isClientSide() && isEmpowered(stack) && world.getGameTime() % 8 == 0) {
            BlockPos pos = entity.blockPosition();
            if (world.isEmptyBlock(pos) && world.getRawBrightness(pos, world.getSkyDarken()) <= LOW_LIGHT_THRESHOLD && useEnergy(stack, true, entity)) {
                placeAir(world, null, pos, 0.3F);
            }
        }
    }

    public void placeAir(Level world, Player player, BlockPos pos, float volume) {

        world.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, volume, 1.0F);
        if (!world.isClientSide()) {
            ((ServerLevel) world).sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.25, 0.25, 0.25, 0);
        }
        world.setBlockAndUpdate(pos, FLUX_GLOW_AIR.get().defaultBlockState());
    }

    public void removeAir(Level world, Player player, BlockPos pos, float volume) {

        world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.0F);
        if (!world.isClientSide()) {
            ((ServerLevel) world).sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.25, 0.25, 0.25, 0);
        }
        world.setBlockAndUpdate(pos, AIR.defaultBlockState());
    }

    protected float getAttackDamage(ItemStack stack) {

        return hasEnergy(stack, false) ? damage : 0.0F;
    }

    protected float getAttackSpeed(ItemStack stack) {

        return attackSpeed;
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
