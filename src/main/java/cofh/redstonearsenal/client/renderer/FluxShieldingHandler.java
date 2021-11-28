package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.util.FluxShieldingHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.gui.ForgeIngameGui;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

public class FluxShieldingHandler {

    protected static final ResourceLocation ICONS = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/gui/flux_shielding_icons.png");

    public static void renderHUD(MatrixStack stack) {

        final Minecraft minecraft = Minecraft.getInstance();
        final IProfiler profiler = minecraft.getProfiler();
        final PlayerEntity player = minecraft.player;

        if (player == null) {
            return;
        }

        profiler.push("fluxShielding");

        int[] charges = FluxShieldingHelper.countCharges(player);
        if (charges[1] <= 0) {
            return;
        }
        minecraft.textureManager.bind(ICONS);
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 10;
        int y = minecraft.getWindow().getGuiScaledHeight() - ForgeIngameGui.right_height;
        ForgeIngameGui.right_height += 10;

        for (int i = charges[0]; i > 0; --i) {
            AbstractGui.blit(stack, x, y, 0, 0, 9, 9, 27, 27);
            x += 8;
        }
        for (int i = charges[1] - charges[0]; i > 0; --i) {
            AbstractGui.blit(stack, x, y, 9, 0, 9, 9, 27, 27);
            x += 8;
        }

        minecraft.textureManager.bind(AbstractGui.GUI_ICONS_LOCATION);
        profiler.pop();
    }

}
