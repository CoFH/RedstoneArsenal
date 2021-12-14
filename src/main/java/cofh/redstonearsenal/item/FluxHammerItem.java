package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.capability.CapabilityAreaEffect;
import cofh.lib.capability.IAreaEffect;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.item.impl.HammerItem;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.AreaEffectHelper;
import cofh.redstonearsenal.entity.ShockwaveEntity;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.constants.Constants.UUID_TOOL_KNOCKBACK;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.GRAY;

public class FluxHammerItem extends HammerItem implements IMultiModeFluxItem {

    protected final float damage;
    protected final float attackSpeed;
    public float knockbackMod = 1.0F;
    public int slamCooldown = 160;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxHammerItem(IItemTier tier, float attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

        super(tier, attackDamageIn, attackSpeedIn, builder);

        this.damage = getAttackDamage();
        this.attackSpeed = attackSpeedIn;

        this.maxEnergy = energy;
        this.extract = xfer;
        this.receive = xfer;

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), this::getChargedModelProperty);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), this::getEmpoweredModelProperty);
    }

    @Override
    @OnlyIn (Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        if (Screen.hasShiftDown() || CoreConfig.alwaysShowDetails) {
            tooltipDelegate(stack, worldIn, tooltip, flagIn);
        } else if (CoreConfig.holdShiftForDetails) {
            tooltip.add(getTextComponent("info.cofh.hold_shift_for_details").withStyle(GRAY));
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
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new FluxHammerItemWrapper(stack, this);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {

        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        int energy = getEnergyPerUse(true) * 2;
        if (player == null || !player.isOnGround() || !isEmpowered(stack) || !(hasEnergy(stack, energy) || player.abilities.instabuild)) {
            return ActionResultType.FAIL;
        }
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        if (!world.isClientSide() && world.addFreshEntity(new ShockwaveEntity(world, player, Vector3d.atCenterOf(pos), player.yRot))) {
            useEnergy(stack, energy, player.abilities.instabuild);
            player.getCooldowns().addCooldown(this, getSlamCooldown(stack));
        }
        world.playSound(player, pos, state.getSoundType(world, pos, player).getBreakSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
        return ActionResultType.SUCCESS;
    }

    protected int getSlamCooldown(ItemStack stack) {

        return slamCooldown;
    }

    protected float getEfficiency(ItemStack stack) {

        return hasEnergy(stack, false) && !isEmpowered(stack) ? speed : 1.0F;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {

        return getToolTypes(stack).stream().anyMatch(state::isToolEffective) ? getEfficiency(stack) : 1.0F;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (useEnergy(stack, isEmpowered(stack), attacker) && isEmpowered(stack)) {
            attacker.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 40, 0));
            if (attacker.hasEffect(Effects.DAMAGE_BOOST)) {
                attacker.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, attacker.getEffect(Effects.DAMAGE_BOOST).getDuration() + 10, 0));
            }
            attacker.setSprinting(true);
        }
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
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {

        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {

        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlotType.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", getAttackSpeed(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID_TOOL_KNOCKBACK, "Tool modifier", getKnockbackModifier(stack), AttributeModifier.Operation.ADDITION));
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
    protected class FluxHammerItemWrapper extends EnergyContainerItemWrapper implements IAreaEffect {

        private final LazyOptional<IAreaEffect> holder = LazyOptional.of(() -> this);

        FluxHammerItemWrapper(ItemStack containerIn, IEnergyContainerItem itemIn) {

            super(containerIn, itemIn, itemIn.getEnergyCapability());
        }

        @Override
        public ImmutableList<BlockPos> getAreaEffectBlocks(BlockPos pos, PlayerEntity player) {

            return AreaEffectHelper.getBreakableBlocksRadius(container, pos, player, 1);
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
