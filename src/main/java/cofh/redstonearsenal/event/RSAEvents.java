package cofh.redstonearsenal.event;

import cofh.lib.util.Utils;
import cofh.redstonearsenal.item.FluxAxeItem;
import cofh.redstonearsenal.item.FluxCrossbowItem;
import cofh.redstonearsenal.item.FluxShovelItem;
import cofh.redstonearsenal.item.FluxTridentItem;
import cofh.redstonearsenal.util.FluxShieldingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.lib.util.references.EnsorcReferences.QUICK_DRAW;
import static cofh.redstonearsenal.capability.CapabilityFluxShielding.FLUX_SHIELDED_ITEM_CAPABILITY;
import static cofh.redstonearsenal.init.RSAReferences.FLUX_PATH;

@Mod.EventBusSubscriber(modid = ID_REDSTONE_ARSENAL)
public class RSAEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleAttackEntityEvent(AttackEntityEvent event) {

        if (!event.isCanceled()) {
            PlayerEntity player = event.getPlayer();
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof FluxTridentItem && player.isAutoSpinAttack()) {
                FluxTridentItem trident = (FluxTridentItem) stack.getItem();
                if (trident.plungeAttack(player.level, player, stack)) {
                    FluxTridentItem.stopSpinAttack(player);
                    player.addEffect(new EffectInstance(Effects.SLOW_FALLING, 35));
                    player.fallDistance = 0;
                    event.getTarget().invulnerableTime = 0;
                    player.attackStrengthTicker = 100;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void handleLivingFallEvent(LivingFallEvent event) {

        LivingEntity living = event.getEntityLiving();
        ItemStack stack = living.getMainHandItem();
        if (stack.getItem() instanceof FluxTridentItem && living.isAutoSpinAttack()) {
            FluxTridentItem trident = (FluxTridentItem) stack.getItem();
            if (trident.plungeAttack(living.level, living, stack)) {
                FluxTridentItem.stopSpinAttack(living);
                if (event.isCancelable()) {
                    event.setCanceled(true);
                }
            } else {
                event.setDamageMultiplier(0.4F);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleCriticalHitEvent(CriticalHitEvent event) {

        if (!event.isCanceled() && event.isVanillaCritical()) {
            ItemStack stack = event.getPlayer().getMainHandItem();
            if (stack.getItem() instanceof FluxAxeItem) {
                ((FluxAxeItem) stack.getItem()).handleCritHit(stack, event);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ServerPlayerEntity && from.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).isPresent() != to.getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).isPresent()) {
            FluxShieldingHelper.updateHUD((ServerPlayerEntity) entity);
        }
        if (from.getItem() instanceof FluxCrossbowItem) {
            EquipmentSlotType slot = event.getSlot();
            //If the used item changes, enforce cooldown
            if ((slot.equals(EquipmentSlotType.MAINHAND) || (slot.equals(EquipmentSlotType.OFFHAND) && !entity.getMainHandItem().isEmpty()))
                    && !(to.getItem() instanceof FluxCrossbowItem
                    && to.getEnchantmentTags().equals(from.getEnchantmentTags()) && from.getBaseRepairCost() == to.getBaseRepairCost())) {
                ((FluxCrossbowItem) from.getItem()).startCooldown(entity, from);
            }
        } else if (event.getSlot().equals(EquipmentSlotType.MAINHAND) && entity.isAutoSpinAttack()
                && from.getItem() instanceof FluxTridentItem && !(to.getItem() instanceof FluxTridentItem)) {
            FluxTridentItem.stopSpinAttack(entity);
        }
    }

    @SubscribeEvent
    public static void handleItemUseTickEvent(LivingEntityUseItemEvent.Tick event) {

        int encQuickDraw = getItemEnchantmentLevel(QUICK_DRAW, event.getItem());
        if (encQuickDraw > 0 && event.getDuration() > event.getItem().getUseDuration() - 20) {
            event.setDuration(event.getDuration() - encQuickDraw);
        }
    }

    @SubscribeEvent
    public static void handleBlockToolInteractEvent(BlockEvent.BlockToolInteractEvent event) {

        ItemStack stack = event.getHeldItemStack();
        BlockState state = event.getState();
        if (stack.getItem() instanceof FluxShovelItem) {
            FluxShovelItem shovel = (FluxShovelItem) stack.getItem();
            if (state.is(Tags.Blocks.DIRT) && shovel.isEmpowered(stack)) {
                event.setFinalState(FLUX_PATH.defaultBlockState());
            } else if (state.is(Blocks.GRASS_PATH) || state.is(FLUX_PATH)) {
                event.setFinalState(Blocks.DIRT.defaultBlockState());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleLivingAttackEvent(LivingAttackEvent event) {

        LivingEntity target = event.getEntityLiving();
        if (!event.isCanceled() && event.getAmount() <= 500.0F && !Utils.isCreativePlayer(target)) {
            ItemStack shieldedItem = FluxShieldingHelper.findShieldedItem(target);
            if (shieldedItem.isEmpty()) {
                return;
            }
            if (target.invulnerableTime > 0) {
                event.setCanceled(true);
            } else if (FluxShieldingHelper.useFluxShieldCharge(target, shieldedItem)) {
                target.invulnerableTime = target.invulnerableDuration;
                event.setCanceled(true);
                if (target instanceof ServerPlayerEntity) {
                    FluxShieldingHelper.updateHUD((ServerPlayerEntity) target);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleLivingHurtEvent(LivingHurtEvent event) {

        LivingEntity target = event.getEntityLiving();
        if (!event.isCanceled() && FluxShieldingHelper.useFluxShieldCharge(target)) {
            event.setAmount(Math.max(event.getAmount() - 500.0F, 0));
            if (target instanceof ServerPlayerEntity) {
                FluxShieldingHelper.updateHUD((ServerPlayerEntity) target);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {

        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
            FluxShieldingHelper.handleHUDSchedule(event.world.getGameTime());
        }
    }

}
