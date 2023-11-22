package cofh.redstonearsenal.common.item;

import cofh.core.common.capability.CapabilityAreaEffect;
import cofh.core.common.config.CoreClientConfig;
import cofh.core.common.item.HammerItem;
import cofh.core.util.ProxyUtils;
import cofh.core.util.helpers.AreaEffectHelper;
import cofh.lib.api.capability.IAreaEffectItem;
import cofh.lib.api.item.IEnergyContainerItem;
import cofh.lib.common.energy.EnergyContainerItemWrapper;
import cofh.lib.util.Utils;
import cofh.redstonearsenal.common.entity.FluxHammerShockwave;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.util.references.EnsorcIDs.ID_EXCAVATING;
import static cofh.lib.util.Constants.UUID_WEAPON_KNOCKBACK;
import static cofh.lib.util.Utils.getEnchantment;
import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.constants.ModIds.ID_ENSORCELLATION;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public class FluxHammerItem extends HammerItem implements IMultiModeFluxItem {

    protected final float damage;
    protected final float attackSpeed;
    public float knockbackMod = 1.0F;
    public int slamCooldown = 160;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxHammerItem(Tier tier, float attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

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
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {

        return new FluxHammerItemWrapper(stack, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        int energy = getEnergyPerUse(true) * 2;
        if (player == null || !player.onGround() || !isEmpowered(stack) || !(hasEnergy(stack, energy) || player.abilities.instabuild)) {
            return InteractionResult.FAIL;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        if (world.isClientSide) {
            world.playSound(player, pos, state.getSoundType(world, pos, player).getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (world.addFreshEntity(new FluxHammerShockwave(world, Vec3.atBottomCenterOf(pos.above()), player))) {
            useEnergy(stack, energy, player.abilities.instabuild);
            player.getCooldowns().addCooldown(this, getSlamCooldown(stack));
        }
        return InteractionResult.SUCCESS;
    }

    protected int getSlamCooldown(ItemStack stack) {

        return slamCooldown;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {

        return hasEnergy(stack, false) && super.canPerformAction(stack, action);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (useEnergy(stack, isEmpowered(stack), attacker) && isEmpowered(stack)) {
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0));
            if (attacker.hasEffect(MobEffects.DAMAGE_BOOST)) {
                attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, attacker.getEffect(MobEffects.DAMAGE_BOOST).getDuration() + 10, 0));
            }
            attacker.setSprinting(true);
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

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {

        return hasEnergy(stack, false) && super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {

        return isCorrectToolForDrops(stack, state) && !isEmpowered(stack) ? speed : 1.0F;
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {

        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {

        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", getAttackSpeed(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID_WEAPON_KNOCKBACK, "Tool modifier", getKnockbackModifier(stack), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    protected float getAttackDamage(ItemStack stack) {

        return hasEnergy(stack, false) ? isEmpowered(stack) && hasEnergy(stack, true) ? damage + 2.0F : damage : 0.0F;
    }

    protected float getAttackSpeed(ItemStack stack) {

        return hasEnergy(stack, false) && isEmpowered(stack) && hasEnergy(stack, true) ? attackSpeed + 0.8F : attackSpeed;
    }

    protected float getKnockbackModifier(ItemStack stack) {

        return knockbackMod;
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
    protected class FluxHammerItemWrapper extends EnergyContainerItemWrapper implements IAreaEffectItem {

        private final LazyOptional<IAreaEffectItem> holder = LazyOptional.of(() -> this);

        FluxHammerItemWrapper(ItemStack containerIn, IEnergyContainerItem itemIn) {

            super(containerIn, itemIn, itemIn.getEnergyCapability());
        }

        @Override
        public ImmutableList<BlockPos> getAreaEffectBlocks(BlockPos pos, Player player) {

            if (hasEnergy(container, false)) {
                return AreaEffectHelper.getBreakableBlocksRadius(container, pos, player, 1 + getItemEnchantmentLevel(getEnchantment(ID_ENSORCELLATION, ID_EXCAVATING), container));
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
}
