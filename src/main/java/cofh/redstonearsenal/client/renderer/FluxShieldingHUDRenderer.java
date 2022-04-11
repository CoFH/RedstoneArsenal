package cofh.redstonearsenal.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

@OnlyIn (Dist.CLIENT)
public class FluxShieldingHUDRenderer {

    protected static final ResourceLocation ICONS = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/gui/flux_shielding_icons.png");
    public static int currCharges = 0;
    public static int maxCharges = 0;

    public static void render(MatrixStack stack) {

        final Minecraft minecraft = Minecraft.getInstance();

        if (maxCharges <= 0) {
            return;
        }
        minecraft.textureManager.bind(ICONS);
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 10;
        int y = minecraft.getWindow().getGuiScaledHeight() - ForgeIngameGui.right_height;
        ForgeIngameGui.right_height += 10;

        for (int i = currCharges; i > 0; --i) {
            AbstractGui.blit(stack, x, y, 0, 0, 9, 9, 27, 27);
            x += 8;
        }
        for (int i = maxCharges - currCharges; i > 0; --i) {
            AbstractGui.blit(stack, x, y, 9, 0, 9, 9, 27, 27);
            x += 8;
        }

        minecraft.textureManager.bind(AbstractGui.GUI_ICONS_LOCATION);
    }

}
