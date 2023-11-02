package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.util.FluxShieldingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

public class FluxShieldingOverlay implements IGuiOverlay {

    public static final ResourceLocation ICONS = new ResourceLocation(ID_REDSTONE_ARSENAL, "textures/gui/flux_shielding_icons.png");

    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {

        if (FluxShieldingHelper.maximumCharges <= 0) {
            return;
        }
        final Minecraft minecraft = gui.getMinecraft();
        if (minecraft.options.hideGui || !gui.shouldDrawSurvivalElements()) {
            return;
        }

        gui.setupOverlayRenderState(true, false);
        int x = width / 2 + 10;
        int y = height - gui.rightHeight;
        gui.rightHeight += 10;

        for (int i = FluxShieldingHelper.currentCharges; i > 0; --i) {
            guiGraphics.blit(ICONS, x, y, 0, 0, 9, 9, 27, 27);
            x += 8;
        }
        for (int i = FluxShieldingHelper.maximumCharges - FluxShieldingHelper.currentCharges; i > 0; --i) {
            guiGraphics.blit(ICONS, x, y, 9, 0, 9, 9, 27, 27);
            x += 8;
        }
    }

}
