package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.util.FluxShieldingHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ForgeIngameGui;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

public class FluxShieldingOverlay {

    protected static final ResourceLocation ICONS = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/gui/flux_shielding_icons.png");

    public static void render(ForgeIngameGui gui, PoseStack stack, float partialTick, int width, int height) {

        if (FluxShieldingHelper.maximumCharges <= 0) {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || !gui.shouldDrawSurvivalElements()) {
            return;
        }

        gui.setupOverlayRenderState(true, false, ICONS);
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 10;
        int y = minecraft.getWindow().getGuiScaledHeight() - gui.right_height;
        gui.right_height += 10;

        for (int i = FluxShieldingHelper.currentCharges; i > 0; --i) {
            GuiComponent.blit(stack, x, y, 0, 0, 9, 9, 27, 27);
            x += 8;
        }
        for (int i = FluxShieldingHelper.maximumCharges - FluxShieldingHelper.currentCharges; i > 0; --i) {
            GuiComponent.blit(stack, x, y, 9, 0, 9, 9, 27, 27);
            x += 8;
        }
    }

}
