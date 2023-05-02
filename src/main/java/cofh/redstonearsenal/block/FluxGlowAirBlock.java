package cofh.redstonearsenal.block;

import cofh.lib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FluxGlowAirBlock extends AirBlock {

    public FluxGlowAirBlock(Properties builder) {

        super(builder);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {

        if (rand.nextInt(8) == 0) {
            Utils.spawnBlockParticlesClient(level, DustParticleOptions.REDSTONE, pos, rand, 2);
        }
    }

}
