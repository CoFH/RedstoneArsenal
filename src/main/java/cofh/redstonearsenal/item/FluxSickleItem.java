package cofh.redstonearsenal.item;

import cofh.core.config.CoreClientConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.block.IHarvestable;
import cofh.lib.capability.CapabilityAreaEffect;
import cofh.lib.capability.IAreaEffect;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.item.ILeftClickHandlerItem;
import cofh.lib.item.impl.SickleItem;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.AreaEffectHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public class FluxSickleItem extends SickleItem implements IMultiModeFluxItem, ILeftClickHandlerItem {

    protected static final Set<Enchantment> VALID_ENCHANTS = new ObjectOpenHashSet<>();
    protected final float damage;
    protected final float attackSpeed;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxSickleItem(Tier tier, float attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

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

        if (Screen.hasShiftDown() || CoreClientConfig.alwaysShowDetails) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreClientConfig.holdShiftForDetails) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(ChatFormatting.GRAY));
        }
    }

    public static void initEnchants() {

        VALID_ENCHANTS.add(Enchantments.SWEEPING_EDGE);
        VALID_ENCHANTS.add(Enchantments.MOB_LOOTING);
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
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {

        return new FluxSickleItemWrapper(stack, this);
    }

    @Override
    public AABB getSweepHitBox(ItemStack stack, Player player, Entity target) {

        return target.getBoundingBox().inflate(radius + 0.25, height + 0.25, radius + 0.25);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {

        return isEmpowered(stack) && entity instanceof LivingEntity living && living.isBaby();
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

        return hasEnergy(stack, false) && EFFECTIVE_MATERIALS.contains(state.getMaterial())
                && (!isEmpowered(stack) || isMature(state));
    }

    protected boolean isMature(BlockState state) {

        Block block = state.getBlock();
        if (block instanceof IHarvestable harvestable) {
            return harvestable.canHarvest(state);
        } else if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        } else if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        } else if (block instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) >= 2;
        } else if (block instanceof ChorusFlowerBlock) {
            return state.getValue(ChorusFlowerBlock.AGE) >= 5;
        } else if (block instanceof LeavesBlock) {
            return !state.getValue(LeavesBlock.PERSISTENT);
        } else if (block instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) >= 3;
        } else if (block instanceof DoublePlantBlock) {
            return !state.is(Blocks.SMALL_DRIPLEAF);
        } else {
            return state.is(BlockTags.TALL_FLOWERS) || block instanceof GrowingPlantBodyBlock || block instanceof BambooBlock;
        }
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {

        return isCorrectToolForDrops(stack, state) ? speed : 0.0F;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {

        return player.isCreative() && !isCorrectToolForDrops(stack, player.level.getBlockState(pos));
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

    // region CAPABILITY WRAPPER
    protected class FluxSickleItemWrapper extends EnergyContainerItemWrapper implements IAreaEffect {

        private final LazyOptional<IAreaEffect> holder = LazyOptional.of(() -> this);

        FluxSickleItemWrapper(ItemStack containerIn, IEnergyContainerItem itemIn) {

            super(containerIn, itemIn, itemIn.getEnergyCapability());
        }

        @Override
        public ImmutableList<BlockPos> getAreaEffectBlocks(BlockPos pos, Player player) {

            if (hasEnergy(container, false)) {
                return AreaEffectHelper.getBlocksCentered(container, pos, player, radius, height);
            }
            return ImmutableList.of();
        }

        // region ICapabilityProvider
        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

            if (cap == CapabilityAreaEffect.AREA_EFFECT_ITEM_CAPABILITY) {
                return CapabilityAreaEffect.AREA_EFFECT_ITEM_CAPABILITY.orEmpty(cap, holder);
            }
            return super.getCapability(cap, side);
        }
        // endregion
    }
    // endregion

    // region ILeftClickHandlerItem
    @Override
    public void onLeftClick(Player player, ItemStack stack) {

        float strength = player.getAttackStrengthScale(0.5F);
        if (strength < 0.9F || player.isSprinting() || player.isSecondaryUseActive() || !hasEnergy(stack, false)) {
            return;
        }
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * (0.2F + strength * strength * 0.8F);
        float sweep = EnchantmentHelper.getSweepingDamageRatio(player);
        boolean empowered = isEmpowered(stack);
        boolean hit = false;
        for(LivingEntity target : player.level.getEntitiesOfClass(LivingEntity.class, stack.getSweepHitBox(player, player))) {
            if (target == player || player.isAlliedTo(target) || (empowered && target.isBaby()) ||
                    (target instanceof ArmorStand stand && stand.isMarker()) || !player.canHit(target, 0)) {
                continue;
            }
            hit = true;
            Vec3 disp = player.position().subtract(target.position());
            target.knockback(0.3F, disp.x, disp.z);
            float bonus = EnchantmentHelper.getDamageBonus(stack, target.getMobType()) * strength;
            target.hurt(DamageSource.playerAttack(player), 1.0F + (damage + bonus) * sweep);
        }
        if (hit) {
            useEnergy(stack, false, player);
        }
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
        player.sweepAttack();
    }
    // endregion

}
