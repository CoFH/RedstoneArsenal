package cofh.redstonearsenal.network.packet.server;

import cofh.core.CoFHCore;
import cofh.lib.network.packet.IPacketServer;
import cofh.lib.network.packet.PacketBase;
import cofh.redstonearsenal.item.FluxSwordItem;
import cofh.redstonearsenal.item.FluxTridentItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import static cofh.lib.util.constants.Constants.PACKET_LEFT_CLICK;

public class LeftClickPacket extends PacketBase implements IPacketServer {

    public LeftClickPacket() {

        super(PACKET_LEFT_CLICK, CoFHCore.PACKET_HANDLER);
    }

    @Override
    public void handleServer(ServerPlayerEntity player) {

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof FluxSwordItem && FluxSwordItem.canSweepAttack(player)) {
            FluxSwordItem sword = (FluxSwordItem) stack.getItem();
            if (sword.isEmpowered(stack)) {
                sword.shootFluxSlash(stack, player);
            }
        } else if (stack.getItem() instanceof FluxTridentItem) {
            FluxTridentItem trident = (FluxTridentItem) stack.getItem();
            if (trident.isEmpowered(stack) && trident.hasEnergy(stack, true)) {
                trident.startPlunge(player);
            }
        }
    }

    @Override
    public void write(PacketBuffer buf) {

    }

    @Override
    public void read(PacketBuffer buf) {

    }

    public static void createAndSend() {

        LeftClickPacket packet = new LeftClickPacket();
        packet.sendToServer();
    }

}
