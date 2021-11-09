package cofh.redstonearsenal.network.packet.server;

import cofh.core.CoFHCore;
import cofh.core.util.control.IReconfigurableTile;
import cofh.lib.network.packet.IPacketServer;
import cofh.lib.network.packet.PacketBase;
import cofh.lib.util.control.IReconfigurable.SideConfig;
import cofh.redstonearsenal.item.FluxSwordItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import static cofh.lib.util.constants.Constants.PACKET_FLUX_SLASH;

public class FluxSlashPacket extends PacketBase implements IPacketServer {

//    protected LivingEntity shooter;

    public FluxSlashPacket() {

        super(PACKET_FLUX_SLASH, CoFHCore.PACKET_HANDLER);
    }

    @Override
    public void handleServer(ServerPlayerEntity player) {

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof FluxSwordItem && FluxSwordItem.canSweepAttack(player)) {
            FluxSwordItem sword = (FluxSwordItem) stack.getItem();
            if (sword.isEmpowered(stack)) {
                sword.shootFluxSlash(stack, player);
            }
        }
    }

    @Override
    public void write(PacketBuffer buf) {

    }

    @Override
    public void read(PacketBuffer buf) {

    }

    public static void sendToServer(LivingEntity shooter) {

        FluxSlashPacket packet = new FluxSlashPacket();
        packet.sendToServer();
    }

}
