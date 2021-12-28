package cofh.redstonearsenal.network.client;

import cofh.core.util.ProxyUtils;
import cofh.lib.network.packet.IPacketClient;
import cofh.lib.network.packet.PacketBase;
import cofh.redstonearsenal.RedstoneArsenal;
import cofh.redstonearsenal.util.FluxShieldingScheduler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

import static cofh.redstonearsenal.init.RSAPackets.PACKET_FLUX_SHIELDING;

public class FluxShieldingPacket extends PacketBase implements IPacketClient {

    protected int currCharges;
    protected int maxCharges;

    public FluxShieldingPacket() {

        super(PACKET_FLUX_SHIELDING, RedstoneArsenal.PACKET_HANDLER);
    }

    @Override
    public void handleClient() {

        if (ProxyUtils.isClient()) {
            FluxShieldingScheduler.updateHUD(currCharges, maxCharges);
        }
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeByte(currCharges);
        buf.writeByte(maxCharges);
    }

    @Override
    public void read(PacketBuffer buf) {

        currCharges = buf.readByte();
        maxCharges = buf.readByte();
    }

    public static void sendToClient(int currCharges, int maxCharges, ServerPlayerEntity player) {

        FluxShieldingPacket packet = new FluxShieldingPacket();
        packet.currCharges = currCharges;
        packet.maxCharges = maxCharges;
        packet.sendToPlayer(player);
    }

    public static void sendToClient(int[] charges, ServerPlayerEntity player) {

        sendToClient(charges[0], charges[1], player);
    }

}
