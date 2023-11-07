package cofh.redstonearsenal.init.registries;

import cofh.redstonearsenal.common.network.packet.client.FluxShieldingPacket;

import static cofh.redstonearsenal.RedstoneArsenal.PACKET_HANDLER;

public class ModPackets {

    public static final int PACKET_FLUX_SHIELDING = 1;

    public static void register() {

        PACKET_HANDLER.registerPacket(PACKET_FLUX_SHIELDING, FluxShieldingPacket::new);
    }

}
