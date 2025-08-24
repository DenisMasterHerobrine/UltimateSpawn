package dev.denismasterherobrine.ultimatespawn.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class ValidSpotChecks {
    public static BlockPos findSafeSpawn(Level world, BlockPos center, int maxRadius) {
        final int minY = 0;
        final int maxY = world.getMaxBuildHeight() - 1;

        for (int r = 0; r <= maxRadius; r += 2) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;

                    int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    if (y < minY || y > maxY) continue;

                    BlockPos feet = new BlockPos(x, y, z);
                    if (isSafe(world, feet)) return feet;
                }
            }
        }

        return null;
    }

    private static boolean isSafe(Level world, BlockPos feet) {
        BlockState below = world.getBlockState(feet.below());
        BlockState at = world.getBlockState(feet);
        BlockState head = world.getBlockState(feet.above());

        if (!below.canOcclude()) return false;
        if (!world.getFluidState(feet).isEmpty()) return false;
        if (!world.getFluidState(feet.above()).isEmpty()) return false;

        return at.getMaterial().isReplaceable() && head.getMaterial().isReplaceable();
    }
}