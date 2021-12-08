package cofh.redstonearsenal.item;

import cofh.core.init.CoreConfig;
import cofh.core.util.ProxyUtils;
import cofh.lib.capability.CapabilityAreaEffect;
import cofh.lib.capability.IAreaEffect;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.item.impl.ExcavatorItem;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.AreaEffectHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
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
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.util.text.TextFormatting.GRAY;

public class FluxExcavatorItem extends ExcavatorItem implements IFluxItem {

    protected final float damage;
    protected final float attackSpeed;

    protected final int maxEnergy;
    protected final int extract;
    protected final int receive;

    public FluxExcavatorItem(IItemTier tier, float attackDamageIn, float attackSpeedIn, Properties builder, int energy, int xfer) {

        super(tier, attackDamageIn, attackSpeedIn, builder);

        this.damage = getAttackDamage();
        this.attackSpeed = attackSpeedIn;

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
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {

        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return getItemEnchantability(stack) > 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new FluxExcavatorItemWrapper(stack, this);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {

        World world = context.getLevel();
        BlockPos clickPos = context.getClickedPos();
        ItemStack tool = context.getItemInHand();
        BlockState state = world.getBlockState(clickPos);
        PlayerEntity player = context.getPlayer();
        if (player == null || !player.mayUseItemAt(clickPos, context.getClickedFace(), tool)) {
            return ActionResultType.PASS;
        }
        ImmutableList<BlockPos> blocks = AreaEffectHelper.getPlaceableBlocksRadius(tool, clickPos, player, 1 + getMode(tool));
        if (blocks.size() < 1) {
            return ActionResultType.FAIL;
        }
        if (!world.isClientSide()) {
            BlockItemUseContext blockContext = new BlockItemUseContext(context);
            BlockPos playerPos = player.blockPosition();
            BlockPos eyePos = new BlockPos(player.getEyePosition(1));
            if (player.abilities.instabuild) {
                for (BlockPos pos : blocks) {
                    BlockPos fillPos = pos.relative(context.getClickedFace());
                    if (world.getBlockState(fillPos).canBeReplaced(blockContext) && !fillPos.equals(playerPos) && !fillPos.equals(eyePos)) {
                        world.setBlock(fillPos, world.getBlockState(pos).getBlock().defaultBlockState(), 2);
                    }
                }
            } else {
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
                for (Block block : sorted.keySet()) {
                    int slot = -1;
                    if (!hasEnergy(context.getItemInHand(), false)) {
                        break;
                    }
                    List<Item> validItems = Block.getDrops(block.defaultBlockState(), (ServerWorld) world, sorted.get(block).get(0), null).stream().map(stack -> stack.getItem()).collect(Collectors.toList());
                    Predicate<ItemStack> matches = stack -> stack.getItem() instanceof BlockItem && (stack.getItem().equals(block.asItem()) || validItems.contains(stack.getItem()));
                    for (BlockPos pos : sorted.get(block)) {
                        if (slot < 0 || inventory.get(slot).isEmpty()) {
                            slot = findFirstInventory(inventory, matches, slot + 1);
                            if (slot < 0) {
                                break;
                            }
                        }
                        if (!useEnergy(context.getItemInHand(), false, false)) {
                            break;
                        }
                        if (world.setBlock(pos, ((BlockItem) inventory.get(slot).getItem()).getBlock().defaultBlockState(), 2)) {
                            inventory.get(slot).shrink(1);
                        }
                    }
                }
            }
        } else {
            world.playLocalSound(clickPos.getX() + 0.5, clickPos.getY() + 0.5, clickPos.getZ() + 0.5, state.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }
        return ActionResultType.sidedSuccess(world.isClientSide());
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

    protected float getEfficiency(ItemStack stack) {

        return hasEnergy(stack, false) ? speed : 1.0F;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {

        return getToolTypes(stack).stream().anyMatch(state::isToolEffective) ? getEfficiency(stack) : 1.0F;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

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

    // region CAPABILITY WRAPPER
    protected class FluxExcavatorItemWrapper extends EnergyContainerItemWrapper implements IAreaEffect {

        private final LazyOptional<IAreaEffect> holder = LazyOptional.of(() -> this);

        FluxExcavatorItemWrapper(ItemStack containerIn, IEnergyContainerItem itemIn) {

            super(containerIn, itemIn, itemIn.getEnergyCapability());
        }

        @Override
        public ImmutableList<BlockPos> getAreaEffectBlocks(BlockPos pos, PlayerEntity player) {

            return AreaEffectHelper.getBreakableBlocksRadius(container, pos, player, 1 + getMode(container));
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
