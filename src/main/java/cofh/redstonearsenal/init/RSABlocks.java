package cofh.redstonearsenal.init;

import cofh.redstonearsenal.block.FluxGlowAirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;

import static cofh.lib.util.helpers.BlockHelper.lightValue;
import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_GLOW_AIR;
import static cofh.redstonearsenal.init.RSAIDs.ID_FLUX_METAL_BLOCK;
import static net.minecraft.block.AbstractBlock.Properties.copy;
import static net.minecraft.block.AbstractBlock.Properties.of;

public class RSABlocks {

    private RSABlocks() {

    }

    public static void register() {

        //BLOCKS.register(ID_FLUX_METAL_BLOCK, () -> new Block(of(Material.METAL, MaterialColor.COLOR_YELLOW).strength(5.0F, 6.0F).sound(SoundType.METAL).harvestLevel(2).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));
        //BLOCKS.register("flux_gem_block", () -> new Block(of(Material.METAL, MaterialColor.COLOR_RED).strength(5.0F, 6.0F).sound(SoundType.METAL).harvestLevel(2).harvestTool(ToolType.PICKAXE).requiresCorrectToolForDrops()));

        BLOCKS.register(ID_FLUX_GLOW_AIR, () -> new FluxGlowAirBlock(copy(Blocks.AIR).lightLevel(lightValue(15))));
    }

}
