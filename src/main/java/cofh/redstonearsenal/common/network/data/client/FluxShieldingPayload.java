package cofh.redstonearsenal.common.network.data.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

public record FluxShieldingPayload(int curCharges, int maxCharges) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(ID_REDSTONE_ARSENAL, "flux_shielding_packet");

    public FluxShieldingPayload(final FriendlyByteBuf buf) {

        this(buf.readInt(), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {

        buf.writeInt(curCharges);
        buf.writeInt(maxCharges);
    }

    @Override
    public ResourceLocation id() {

        return ID;
    }

}
