package cofh.redstonearsenal.event;

import cofh.redstonearsenal.item.FluxAxeItem;
import cofh.redstonearsenal.item.FluxSwordItem;
import cofh.redstonearsenal.network.packet.server.FluxSlashPacket;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

@Mod.EventBusSubscriber(modid = ID_REDSTONE_ARSENAL)
public class RSAEvents {

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
    public static void handleAttackEntityEvent(AttackEntityEvent event) {

        if (!event.isCanceled()) {
            ItemStack stack = event.getPlayer().getMainHandItem();
            if (stack.getItem() instanceof FluxSwordItem && FluxSwordItem.canSweepAttack(event.getPlayer())) {
                FluxSwordItem sword = (FluxSwordItem) stack.getItem();
                if (sword.isEmpowered(stack)) {
                    sword.shootFluxSlash(stack, event.getPlayer());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleLeftClickBlock(LeftClickBlock event) {

        if (!event.isCanceled()) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() instanceof FluxSwordItem && FluxSwordItem.canSweepAttack(event.getPlayer())) {
                FluxSwordItem sword = (FluxSwordItem) stack.getItem();
                if (sword.isEmpowered(stack)) {
                    sword.shootFluxSlash(stack, event.getPlayer());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleLeftClickEmpty(LeftClickEmpty event) {

        if (!event.isCanceled()) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() instanceof FluxSwordItem) {
                (new FluxSlashPacket()).sendToServer();
            }
        }
    }
}
