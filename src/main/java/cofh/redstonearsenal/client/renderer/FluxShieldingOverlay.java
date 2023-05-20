package cofh.redstonearsenal.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

public class FluxShieldingOverlay {

    protected static final ResourceLocation ICONS = new ResourceLocation(ID_REDSTONE_ARSENAL, "textures/gui/flux_shielding_icons.png");
    public static int currCharges = 0;
    public static int maxCharges = 0;

    public static void render(ForgeGui gui, PoseStack stack, float partialTick, int width, int height) {

        if (maxCharges <= 0) {
            return;
        }
        final Minecraft minecraft = gui.getMinecraft();
        if (minecraft.options.hideGui || !gui.shouldDrawSurvivalElements()) {
            return;
        }

        gui.setupOverlayRenderState(true, false, ICONS);
        int x = width / 2 + 10;
        int y = height - gui.rightHeight;
        gui.rightHeight += 10;

        for (int i = currCharges; i > 0; --i) {
            GuiComponent.blit(stack, x, y, 0, 0, 9, 9, 27, 27);
            x += 8;
        }
        for (int i = maxCharges - currCharges; i > 0; --i) {
            GuiComponent.blit(stack, x, y, 9, 0, 9, 9, 27, 27);
            x += 8;
        }
    }

}
