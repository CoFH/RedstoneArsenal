package cofh.redstonearsenal.item;

import cofh.core.capability.CapabilityAreaEffect;
import cofh.core.config.CoreClientConfig;
import cofh.core.item.ExcavatorItem;
import cofh.core.util.ProxyUtils;
import cofh.core.util.helpers.AreaEffectHelper;
import cofh.lib.api.capability.IAreaEffectItem;
import cofh.lib.api.item.IEnergyContainerItem;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static cofh.core.util.references.EnsorcIDs.ID_EXCAVATING;
import static cofh.lib.util.Utils.getEnchantment;
import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.constants.ModIds.ID_ENSORCELLATION;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;

public class FluxExcavatorItem extends ExcavatorItem implements IMultiModeFluxItem {

    protected final float damage;
    protected final float attackSpeed;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxExcavatorItem(Tier tier, float attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

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

        return new FluxExcavatorItemWrapper(stack, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Level world = context.getLevel();
        BlockPos clickPos = context.getClickedPos();
        ItemStack tool = context.getItemInHand();
        BlockState state = world.getBlockState(clickPos);
        Player player = context.getPlayer();
        if (player == null || !player.mayUseItemAt(clickPos, context.getClickedFace(), tool)) {
            return InteractionResult.PASS;
        }
        ImmutableList<BlockPos> blocks = AreaEffectHelper.getPlaceableBlocksRadius(tool, clickPos, player, 1 + getMode(tool));
        if (blocks.size() < 1) {
            return InteractionResult.FAIL;
        }
        if (world.isClientSide) {
            world.playLocalSound(clickPos.getX() + 0.5, clickPos.getY() + 0.5, clickPos.getZ() + 0.5, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
        } else {
            BlockPlaceContext blockContext = new BlockPlaceContext(context);
            BlockPos playerPos = player.blockPosition();
            BlockPos eyePos = new BlockPos(player.getEyePosition(1));
            if (player.abilities.instabuild) {
                for (BlockPos pos : blocks) {
                    BlockPos fillPos = pos.relative(context.getClickedFace());
                    if (world.getBlockState(fillPos).canBeReplaced(blockContext) && !fillPos.equals(playerPos) && !fillPos.equals(eyePos)) {
                        world.setBlock(fillPos, world.getBlockState(pos).getBlock().defaultBlockState(), 2);
                    }
                }
            } else if (hasEnergy(tool, false)) {
                Map<Block, List<BlockPos>> sorted = new HashMap<>();
                for (BlockPos pos : blocks) {
                    BlockPos fillPos = pos.relative(context.getClickedFace());
                    if (world.getBlockState(fillPos).canBeReplaced(blockContext) && !fillPos.equals(playerPos) && !fillPos.equals(eyePos)) {
                        Block block = world.getBlockState(pos).getBlock();
                        sorted.putIfAbsent(block, new ArrayList<>());
                        sorted.get(block).add(fillPos);
                    }
                }
                NonNullList<ItemStack> inventory = player.inventory.items;
                int energyPer = getEnergyPerUse(false) / 2;
                int energyStored = getEnergyStored(tool);
                int energyUse = 0;
                for (Block block : sorted.keySet()) {
                    if (energyUse >= energyStored) {
                        break;
                    }
                    List<BlockPos> posns = sorted.get(block);
                    List<Item> validItems = Block.getDrops(block.defaultBlockState(), (ServerLevel) world, posns.get(0), null).stream().map(ItemStack::getItem).filter(i -> i instanceof BlockItem).collect(Collectors.toList());
                    Predicate<ItemStack> matches = stack -> stack.getItem() instanceof BlockItem && (stack.getItem().equals(block.asItem()) || validItems.contains(stack.getItem()));
                    int slot = -1;
                    int uses = posns.size() - 1;
                    while (uses > 0) {
                        slot = findFirstInventory(inventory, matches, slot + 1);
                        if (slot < 0) {
                            break;
                        }
                        ItemStack stack = inventory.get(slot);
                        BlockState place = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                        int count = uses - stack.getCount();
                        int consume = 0;
                        for (; uses > count && uses >= 0; --uses) {
                            if (world.setBlock(posns.get(uses), place, 2)) {
                                ++consume;
                                energyUse += energyPer;
                                if (energyUse > energyStored) {
                                    break;
                                }
                            }
                        }
                        stack.shrink(consume);
                    }
                }
                useEnergy(tool, Math.min(energyUse, energyStored), false);
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide());
    }

    public static int findFirstInventory(NonNullList<ItemStack> inventory, Predicate<ItemStack> filter, int start) {

        for (int i = start; i < inventory.size(); ++i) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty() && filter.test(stack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {

        return hasEnergy(stack, false) && super.canPerformAction(stack, action) && action != ToolActions.SHOVEL_FLATTEN;
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

        return hasEnergy(stack, false) && (super.isCorrectToolForDrops(stack, state) || state.getBlock().equals(Blocks.POWDER_SNOW) || state.is(BlockTags.FIRE));
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
    protected class FluxExcavatorItemWrapper extends EnergyContainerItemWrapper implements IAreaEffectItem {

        private final LazyOptional<IAreaEffectItem> holder = LazyOptional.of(() -> this);

        FluxExcavatorItemWrapper(ItemStack containerIn, IEnergyContainerItem itemIn) {

            super(containerIn, itemIn, itemIn.getEnergyCapability());
        }

        @Override
        public ImmutableList<BlockPos> getAreaEffectBlocks(BlockPos pos, Player player) {

            if (hasEnergy(container, false)) {
                return AreaEffectHelper.getBreakableBlocksRadius(container, pos, player, 1 + getMode(container) + getItemEnchantmentLevel(getEnchantment(ID_ENSORCELLATION, ID_EXCAVATING), container));
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
