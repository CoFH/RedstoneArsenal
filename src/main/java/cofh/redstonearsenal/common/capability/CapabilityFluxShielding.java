package cofh.redstonearsenal.common.capability;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.ItemCapability;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

public final class CapabilityFluxShielding {

    public static final ItemCapability<IFluxShieldedItem, Void> ITEM = ItemCapability.createVoid(new ResourceLocation(ID_REDSTONE_ARSENAL, "flux_shielding"), IFluxShieldedItem.class);

    private CapabilityFluxShielding() {

    }

}
