package cofh.redstonearsenal.common.network.packet.client;

import cofh.redstonearsenal.common.network.data.client.FluxShieldingPayload;
import cofh.redstonearsenal.util.FluxShieldingHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class FluxShieldingPacket {

    public static final FluxShieldingPacket INSTANCE = new FluxShieldingPacket();

    public static FluxShieldingPacket get() {

        return INSTANCE;
    }

    public void handle(final FluxShieldingPayload payload, final PlayPayloadContext context) {

        context.workHandler().submitAsync(() -> FluxShieldingHelper.updateHUD(payload.curCharges(), payload.maxCharges()));
    }

    public static void sendToClient(int curCharges, int maxCharges, ServerPlayer player) {

        PacketDistributor.PLAYER.with(player).send(new FluxShieldingPayload(curCharges, maxCharges));
    }

    public static void sendToClient(int[] charges, ServerPlayer player) {

        sendToClient(charges[0], charges[1], player);
    }

}
