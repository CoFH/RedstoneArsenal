package cofh.redstonearsenal.block;

import cofh.lib.util.Utils;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class FluxGlowAirBlock extends AirBlock {

    public FluxGlowAirBlock(Properties builder) {

        super(builder);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {

        if (rand.nextInt(8) == 0) {
            Utils.spawnBlockParticlesClient(worldIn, RedstoneParticleData.REDSTONE, pos, rand, 2);
        }
    }

}
