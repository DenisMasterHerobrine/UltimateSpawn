package dev.denismasterherobrine.ultimatespawn.utils;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

public class ValidSpotChecks {
    public static BlockPos findSafeSpawn(ServerWorld world, BlockPos center, int maxRadius) {
        final int minY = 0;
        final int maxY = world.getMaxBuildHeight() - 1;

        for (int r = 0; r <= maxRadius; r += 2) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;

                    int y = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
                    if (y < minY || y > maxY) continue;

                    BlockPos feet = new BlockPos(x, y, z);
                    if (isSafe(world, feet)) return feet;
                }
            }
        }

        return null;
    }

    private static boolean isSafe(World world, BlockPos feet) {
        BlockState below = world.getBlockState(feet.below());
        BlockState at = world.getBlockState(feet);
        BlockState head = world.getBlockState(feet.above());

        if (!below.canOcclude()) return false;
        if (!world.getFluidState(feet).isEmpty()) return false;
        if (!world.getFluidState(feet.above()).isEmpty()) return false;

        return at.getMaterial().isReplaceable() && head.getMaterial().isReplaceable();
    }
}