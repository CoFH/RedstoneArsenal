package cofh.redstonearsenal.util;

import cofh.thermal.lib.common.ThermalFlags;
import cofh.thermal.lib.common.ThermalIDs;

public class ThermalCoreCompat {
    public static void setFeatureFlags() {
        ThermalFlags.setFlag(ThermalIDs.ID_CHARGE_BENCH, true);
    }
}
