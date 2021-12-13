package cofh.redstonearsenal.compat.curios;

import cofh.redstonearsenal.network.client.FluxShieldingPacket;
import cofh.redstonearsenal.util.FluxShieldingHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import static cofh.redstonearsenal.capability.CapabilityFluxShielding.FLUX_SHIELDED_ITEM_CAPABILITY;

public class CuriosEvents {

    public static void register() {

        MinecraftForge.EVENT_BUS.addListener(CuriosEvents::handleCurioChangeEvent);
    }

    public static void handleCurioChangeEvent(CurioChangeEvent event) {

        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ServerPlayerEntity && event.getFrom().getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).isPresent() != event.getTo().getCapability(FLUX_SHIELDED_ITEM_CAPABILITY).isPresent()) {
            FluxShieldingPacket.sendToClient(FluxShieldingHelper.countCharges(entity), (ServerPlayerEntity) entity);
        }
    }

}
