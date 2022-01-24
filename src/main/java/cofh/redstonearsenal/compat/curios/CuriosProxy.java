package cofh.redstonearsenal.compat.curios;

import net.minecraftforge.common.MinecraftForge;

public class CuriosProxy {

    public static void register() {

        MinecraftForge.EVENT_BUS.addListener(CuriosEvents::handleCurioChangeEvent);
    }

}
