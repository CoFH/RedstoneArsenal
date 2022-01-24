package cofh.redstonearsenal.init;

import cofh.redstonearsenal.block.FluxGlowAirBlock;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;

import static cofh.lib.util.helpers.BlockHelper.lightValue;
import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
import static cofh.redstonearsenal.init.RSAIDs.*;
import static net.minecraft.block.AbstractBlock.Properties.copy;
import static net.minecraft.block.AbstractBlock.Properties.of;

public class RSABlocks {

    private RSABlocks() {

    }

    public static void register() {

        BLOCKS.register(ID_FLUX_METAL_BLOCK, () -> new Block(of(Material.METAL, MaterialColor.COLOR_YELLOW).strength(5.0F, 6.0F).sound(SoundType.METAL).harvestLevel(2).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        BLOCKS.register(ID_FLUX_GEM_BLOCK, () -> new Block(of(Material.METAL, MaterialColor.COLOR_RED).strength(5.0F, 6.0F).sound(SoundType.METAL).harvestLevel(2).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        BLOCKS.register(ID_FLUX_GLOW_AIR, () -> new FluxGlowAirBlock(copy(Blocks.AIR).lightLevel(lightValue(15))));
        BLOCKS.register(ID_FLUX_PATH, () -> new GrassPathBlock(AbstractBlock.Properties.of(Material.DIRT).strength(0.65F).harvestTool(ToolType.SHOVEL).sound(SoundType.GRASS).isViewBlocking((a, b, c) -> true).isSuffocating((a, b, c) -> true).speedFactor(1.12F).friction(0.56F).lightLevel(lightValue(4))));
    }

}
