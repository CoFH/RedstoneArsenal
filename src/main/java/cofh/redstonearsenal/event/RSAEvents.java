package cofh.redstonearsenal.event;

import cofh.core.event.ShieldEvents;
import cofh.lib.util.Utils;
import cofh.redstonearsenal.item.FluxAxeItem;
import cofh.redstonearsenal.item.FluxShieldItem;
import cofh.redstonearsenal.item.FluxShovelItem;
import cofh.redstonearsenal.item.FluxTridentItem;
import cofh.redstonearsenal.util.FluxShieldingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.capability.CapabilityShieldItem.SHIELD_ITEM_CAPABILITY;
import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.init.RSAReferences.FLUX_PATH;
import static net.minecraft.inventory.EquipmentSlot.MAINHAND;
import static net.minecraft.world.entity.EquipmentSlot.MAINHAND;

@Mod.EventBusSubscriber (modid = ID_REDSTONE_ARSENAL)
public class RSAEvents {

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleAttackEntityEvent(AttackEntityEvent event) {

        if (event.isCanceled()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack stack = player.getMainHandItem();
        // Flux Trident
        if (stack.getItem() instanceof FluxTridentItem && player.isAutoSpinAttack()) {
            FluxTridentItem trident = (FluxTridentItem) stack.getItem();
            if (trident.plungeAttack(player.level, player, stack)) {
                event.getTarget().invulnerableTime = 0;
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.HIGH)
    public static void handleLivingFallEvent(LivingFallEvent event) {

        if (event.getEntityLiving() instanceof Player) {
            // Flux Trident
            LivingEntity living = event.getEntityLiving();
            ItemStack stack = living.getMainHandItem();
            if (stack.getItem() instanceof FluxTridentItem && living.isAutoSpinAttack()) {
                FluxTridentItem trident = (FluxTridentItem) stack.getItem();
                if (trident.plungeAttack(living.level, living, stack)) {
                    FluxTridentItem.stopSpinAttack(living);
                    event.setCanceled(true);
                } else {
                    event.setDamageMultiplier(0.4F);
                }
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.HIGH)
    public static void handlePlayerFlyableFallEvent(PlayerFlyableFallEvent event) {

        // Flux Trident
        Player player = event.getPlayer();
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof FluxTridentItem && player.isAutoSpinAttack()) {
            FluxTridentItem trident = (FluxTridentItem) stack.getItem();
            if (trident.plungeAttack(player.level, player, stack)) {
                FluxTridentItem.stopSpinAttack(player);
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleCriticalHitEvent(CriticalHitEvent event) {

        // Flux Axe
        if (event.isCanceled() || !event.isVanillaCritical()) {
            return;
        }
        ItemStack stack = event.getPlayer().getMainHandItem();
        if (stack.getItem() instanceof FluxAxeItem) {
            ((FluxAxeItem) stack.getItem()).handleCritHit(stack, event);
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();
        LivingEntity entity = event.getEntityLiving();
        if (event.getSlot().equals(MAINHAND) && entity.isAutoSpinAttack()
                && from.getItem() instanceof FluxTridentItem && !(to.getItem() instanceof FluxTridentItem)) { //Flux Trident
            FluxTridentItem.stopSpinAttack(entity);
        }
    }

    @SubscribeEvent
    public static void handleBlockToolInteractEvent(BlockEvent.BlockToolInteractEvent event) {

        if (event.isCanceled()) {
            return;
        }
        // Flux Shovel
        ItemStack stack = event.getHeldItemStack();
        BlockState state = event.getState();
        if (stack.getItem() instanceof FluxShovelItem) {
            FluxShovelItem shovel = (FluxShovelItem) stack.getItem();
            if (state.is(Blocks.GRASS_PATH) || state.is(FLUX_PATH) || state.is(Blocks.FARMLAND)) {
                event.setFinalState(Blocks.DIRT.defaultBlockState());
                //} else if (state.getBlock() instanceof TilledSoilBlock) { //TODO: Thermal phyto-soil
            } else if (shovel.isEmpowered(stack)) {
                if (state.is(Tags.Blocks.DIRT)) {
                    event.setFinalState(FLUX_PATH.defaultBlockState());
                }
            } else if (state.is(Blocks.DIRT)) { //TODO: configurable?
                event.setFinalState(Blocks.GRASS_PATH.defaultBlockState());
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleLivingAttackEvent(LivingAttackEvent event) {

        LivingEntity target = event.getEntityLiving();
        DamageSource source = event.getSource();
        if (event.isCanceled() || ShieldEvents.canBlockDamageSource(target, source)) {
            return;
        }
        // Flux Shield
        if (target.isBlocking() && source.getMsgId().equals("flux")) {
            ItemStack shield = target.getUseItem();
            if (shield.getItem() instanceof FluxShieldItem && ShieldEvents.canBlockDamagePosition(target, source.getSourcePosition())) {
                shield.getCapability(SHIELD_ITEM_CAPABILITY).ifPresent(cap -> cap.onBlock(target, source, event.getAmount()));
                event.setCanceled(true);
                return;
            }
        }

        // Flux Shielding
        if (event.getAmount() > 500.0F || Utils.isCreativePlayer(target) || target.isInvulnerableTo(source) || (target.hasEffect(MobEffects.FIRE_RESISTANCE) && source.isFire())) {
            return;
        }
        ItemStack shieldedItem = FluxShieldingHelper.findShieldedItem(target);
        if (shieldedItem.isEmpty()) {
            return;
        }
        if (target.invulnerableTime > 0) {
            event.setCanceled(true);
        } else if (FluxShieldingHelper.useFluxShieldCharge(target, shieldedItem)) {
            target.invulnerableTime = 10;
            event.setCanceled(true);
            if (target instanceof ServerPlayer) {
                FluxShieldingHelper.updateHUD((ServerPlayer) target);
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleLivingHurtEvent(LivingHurtEvent event) {

        if (event.isCanceled()) {
            return;
        }
        // Flux Shielding
        LivingEntity target = event.getEntityLiving();
        float amount = event.getAmount();
        if (amount > 0.0F && FluxShieldingHelper.useFluxShieldCharge(target)) {
            event.setAmount(Math.max(amount - 500.0F, 0));
            if (target instanceof ServerPlayer) {
                FluxShieldingHelper.updateHUD((ServerPlayer) target);
            }
        }
    }

}
