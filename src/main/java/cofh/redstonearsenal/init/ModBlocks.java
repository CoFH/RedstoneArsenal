package cofh.redstonearsenal.init;

import cofh.redstonearsenal.block.FluxGlowAirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

import static cofh.lib.util.helpers.BlockHelper.lightValue;
import static cofh.redstonearsenal.RedstoneArsenal.BLOCKS;
import static cofh.redstonearsenal.init.ModIDs.*;
import static net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy;
import static net.minecraft.world.level.block.state.BlockBehaviour.Properties.of;

public class ModBlocks {

    private ModBlocks() {

    }

    public static void register() {

    }

    public static final RegistryObject<Block> FLUX_METAL = BLOCKS.register(ID_FLUX_METAL_BLOCK, () -> new Block(of().mapColor(MapColor.COLOR_YELLOW).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> FLUX_GEM = BLOCKS.register(ID_FLUX_GEM_BLOCK, () -> new Block(of().mapColor(MapColor.COLOR_RED).strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> FLUX_GLOW_AIR = BLOCKS.register(ID_FLUX_GLOW_AIR, () -> new FluxGlowAirBlock(copy(Blocks.AIR).lightLevel(lightValue(15))));
    public static final RegistryObject<Block> FLUX_PATH = BLOCKS.register(ID_FLUX_PATH, () -> new DirtPathBlock(of().strength(0.65F).sound(SoundType.GRASS).isViewBlocking((a, b, c) -> true).isSuffocating((a, b, c) -> true).speedFactor(1.12F).friction(0.56F).lightLevel(lightValue(4))));

}
