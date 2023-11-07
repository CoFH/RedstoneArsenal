package cofh.redstonearsenal.common.network.packet.client;

import cofh.core.util.ProxyUtils;
import cofh.lib.common.network.packet.IPacketClient;
import cofh.lib.common.network.packet.PacketBase;
import cofh.redstonearsenal.RedstoneArsenal;
import cofh.redstonearsenal.util.FluxShieldingHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import static cofh.redstonearsenal.init.registries.ModPackets.PACKET_FLUX_SHIELDING;

public class FluxShieldingPacket extends PacketBase implements IPacketClient {

    protected int currCharges;
    protected int maxCharges;

    public FluxShieldingPacket() {

        super(PACKET_FLUX_SHIELDING, RedstoneArsenal.PACKET_HANDLER);
    }

    @Override
    public void handleClient() {

        if (ProxyUtils.isClient()) {
            FluxShieldingHelper.updateHUD(currCharges, maxCharges);
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {

        buf.writeByte(currCharges);
        buf.writeByte(maxCharges);
    }

    @Override
    public void read(FriendlyByteBuf buf) {

        currCharges = buf.readByte();
        maxCharges = buf.readByte();
    }

    public static void sendToClient(int currCharges, int maxCharges, ServerPlayer player) {

        FluxShieldingPacket packet = new FluxShieldingPacket();
        packet.currCharges = currCharges;
        packet.maxCharges = maxCharges;
        packet.sendToPlayer(player);
    }

    public static void sendToClient(int[] charges, ServerPlayer player) {

        sendToClient(charges[0], charges[1], player);
    }

}
