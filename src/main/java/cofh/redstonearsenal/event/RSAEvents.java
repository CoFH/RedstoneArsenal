package cofh.redstonearsenal.event;

import cofh.core.event.ShieldEvents;
import cofh.lib.util.Utils;
import cofh.redstonearsenal.item.FluxArmorItem;
import cofh.redstonearsenal.item.FluxShieldItem;
import cofh.redstonearsenal.item.FluxShovelItem;
import cofh.redstonearsenal.item.FluxTridentItem;
import cofh.redstonearsenal.util.FluxShieldingHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.capability.CapabilityShieldItem.SHIELD_ITEM_CAPABILITY;
import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.init.RSAReferences.FLUX_PATH;
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
        if (stack.getItem() instanceof FluxTridentItem trident && player.isAutoSpinAttack()) {
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
            if (stack.getItem() instanceof FluxTridentItem trident && living.isAutoSpinAttack()) {
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
        if (stack.getItem() instanceof FluxTridentItem trident && player.isAutoSpinAttack()) {
            if (trident.plungeAttack(player.level, player, stack)) {
                FluxTridentItem.stopSpinAttack(player);
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();
        LivingEntity entity = event.getEntityLiving();
        //Flux Trident
        if (event.getSlot().equals(MAINHAND) && entity.isAutoSpinAttack()
                && from.getItem() instanceof FluxTridentItem && !(to.getItem() instanceof FluxTridentItem)) {
            FluxTridentItem.stopSpinAttack(entity);
        }
    }

    @SubscribeEvent (priority = EventPriority.HIGHEST)
    public static void handleUnchargedBlockToolModificationEvent(BlockEvent.BlockToolModificationEvent event) {

        // TODO: Event missing Cancelable annotation in 1.18. Revisit in 1.19.
        //if (event.isCanceled()) {
        //    return;
        //}
        // Cancel modification if no energy
        //ItemStack stack = event.getHeldItemStack();
        //if (stack.getItem() instanceof IFluxItem tool) {
        //    event.setCanceled(!(Utils.isCreativePlayer(event.getPlayer()) || tool.hasEnergy(stack, false)));
        //}
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleBlockToolModificationEvent(BlockEvent.BlockToolModificationEvent event) {

        if (event.isCanceled()) {
            return;
        }
        // Flux Shovel
        ToolAction action = event.getToolAction();
        if (!action.equals(ToolActions.SHOVEL_FLATTEN)) {
            return;
        }
        ItemStack stack = event.getHeldItemStack();
        if (stack.getItem() instanceof FluxShovelItem shovel) {
            BlockState state = event.getState();
            if (state.is(Blocks.DIRT_PATH) || state.is(FLUX_PATH) || state.is(Blocks.FARMLAND)) {
                event.setFinalState(Blocks.DIRT.defaultBlockState());
            } else if (shovel.isEmpowered(stack)) {
                BlockState modified = state.getBlock().getToolModifiedState(state, event.getContext(), action, event.isSimulated());
                if (modified != null && modified.is(Blocks.DIRT_PATH)){ //event.getFinalState().is(Blocks.DIRT_PATH) ||
                    event.setFinalState(FLUX_PATH.defaultBlockState());
                }
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleLivingAttackEvent(LivingAttackEvent event) {

        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntityLiving();
        DamageSource source = event.getSource();
        if (ShieldEvents.canBlockDamageSource(target, source) || target.isInvulnerableTo(source) ||
                (target.hasEffect(MobEffects.FIRE_RESISTANCE) && source.isFire())) {
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
        float amount = event.getAmount();
        if (amount <= 500.0F && !Utils.isCreativePlayer(target) && !(source.isBypassArmor() && source.isBypassMagic())) {
            ItemStack shieldedItem = FluxShieldingHelper.findShieldedItem(target);
            if (!shieldedItem.isEmpty()) {
                if (target.invulnerableTime > 0) {
                    event.setCanceled(true);
                } else if (FluxShieldingHelper.useFluxShieldCharge(target, shieldedItem)) {
                    target.invulnerableTime = 10;
                    event.setCanceled(true);
                    if (target instanceof ServerPlayer) {
                        FluxShieldingHelper.updateHUD((ServerPlayer) target);
                    }
                }
                return;
            }
        }

        // Flux Armor Helmet Damage
        if (source.isDamageHelmet()) {
            ItemStack helmet = target.getItemBySlot(EquipmentSlot.HEAD);
            float damage = Math.max(0.5F, amount * 0.25F);
            if (helmet.getItem() instanceof FluxArmorItem armor) {
                int use = Math.min((int) (damage * armor.getEnergyPerUse(false)), armor.getEnergyStored(helmet));
                armor.useEnergy(helmet, use, target);
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleLivingHurtEvent(LivingHurtEvent event) {

        if (event.isCanceled()) {
            return;
        }
        // Flux Shielding
        DamageSource source = event.getSource();
        if (source.isBypassArmor() && source.isBypassMagic()) {
            return;
        }
        float amount = event.getAmount();
        if (amount <= 0.0F) {
            return;
        }
        LivingEntity target = event.getEntityLiving();
        if (FluxShieldingHelper.useFluxShieldCharge(target)) {
            event.setAmount(Math.max(amount - 500.0F, 0));
            if (target instanceof ServerPlayer) {
                FluxShieldingHelper.updateHUD((ServerPlayer) target);
            }
        } else if (!source.isBypassArmor()) { // Flux Armor Damage
            float damage = Math.max(0.5F, amount * 0.25F);
            target.getArmorSlots().forEach(stack -> {
                if (stack.getItem() instanceof FluxArmorItem armor) {
                    int use = Math.min((int) (damage * armor.getEnergyPerUse(false)), armor.getEnergyStored(stack));
                    armor.useEnergy(stack, use, target);
                }
            });
        }
    }

}
