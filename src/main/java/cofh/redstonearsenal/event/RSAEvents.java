package cofh.redstonearsenal.event;

import cofh.redstonearsenal.item.FluxAxeItem;
import cofh.redstonearsenal.item.FluxCrossbowItem;
import cofh.redstonearsenal.item.FluxTridentItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

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
        if (from.getItem() instanceof FluxCrossbowItem) {
            EquipmentSlotType slot = event.getSlot();
            //If the used item changes, enforce cooldown
            if ((slot.equals(EquipmentSlotType.MAINHAND) || (slot.equals(EquipmentSlotType.OFFHAND) && !event.getEntityLiving().getMainHandItem().isEmpty()))
                    && !(to.getItem() instanceof FluxCrossbowItem
                    && to.getEnchantmentTags().equals(from.getEnchantmentTags()) && from.getBaseRepairCost() == to.getBaseRepairCost())) {
                ((FluxCrossbowItem) from.getItem()).startCooldown(event.getEntityLiving(), from);
            }
        } else if (event.getSlot().equals(EquipmentSlotType.MAINHAND) && event.getEntityLiving().isAutoSpinAttack()
                && from.getItem() instanceof FluxTridentItem && !(to.getItem() instanceof FluxTridentItem)) {
            FluxTridentItem.stopSpinAttack(event.getEntityLiving());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleStopEvent(LivingEntityUseItemEvent.Stop event) {

        ItemStack stack = event.getItem();
        if (stack.getItem() instanceof FluxCrossbowItem) {
            ((FluxCrossbowItem) stack.getItem()).startCooldown(event.getEntityLiving(), stack);
        }
    }

}
