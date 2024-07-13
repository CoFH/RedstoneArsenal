package cofh.redstonearsenal.common.network;

import cofh.redstonearsenal.common.network.data.client.FluxShieldingPayload;
import cofh.redstonearsenal.common.network.packet.client.FluxShieldingPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

public class PacketHandler {

    public static void registerNetworking(final RegisterPayloadHandlerEvent event) {

        final IPayloadRegistrar registrar = event.registrar(ID_REDSTONE_ARSENAL);

        // CLIENT
        registrar.play(FluxShieldingPayload.ID, FluxShieldingPayload::new, handler -> handler.client(FluxShieldingPacket.get()::handle));
    }

}
