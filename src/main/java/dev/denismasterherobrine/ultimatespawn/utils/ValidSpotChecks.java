package dev.denismasterherobrine.ultimatespawn.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;

public final class ValidSpotChecks {
    private static final Heightmap.Types MOTION_HEIGTMAP = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;

    private ValidSpotChecks() {}

    @Nullable
    public static BlockPos validPlayerSpawnLocation(Level level, BlockPos center, int maxRadius) {
        final int minY = level.getMinBuildHeight();
        final int maxY = level.getMaxBuildHeight() - 1;

        BlockPos quick = evaluateColumn(level, center.getX(), center.getZ(), minY, maxY);
        if (quick != null) return quick;

        final WorldBorder border = level.getWorldBorder();
        final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();

        for (int r = 1; r <= maxRadius; r++) {
            final int x0 = center.getX() - r;
            final int x1 = center.getX() + r;
            final int z0 = center.getZ() - r;
            final int z1 = center.getZ() + r;

            for (int x = x0; x <= x1; x++) {
                probe.set(x, minY, z0);
                if (border.isWithinBounds(probe)) {
                    BlockPos pos = evaluateColumn(level, x, z0, minY, maxY);
                    if (pos != null) return pos;
                }

                if (z1 != z0) {
                    probe.set(x, minY, z1);
                    if (border.isWithinBounds(probe)) {
                        BlockPos pos = evaluateColumn(level, x, z1, minY, maxY);
                        if (pos != null) return pos;
                    }
                }
            }

            for (int z = z0 + 1; z <= z1 - 1; z++) {
                probe.set(x0, minY, z);
                if (border.isWithinBounds(probe)) {
                    BlockPos pos = evaluateColumn(level, x0, z, minY, maxY);
                    if (pos != null) return pos;
                }

                probe.set(x1, minY, z);
                if (border.isWithinBounds(probe)) {
                    BlockPos pos = evaluateColumn(level, x1, z, minY, maxY);
                    if (pos != null) return pos;
                }
            }
        }

        return null;
    }

    @Nullable
    private static BlockPos evaluateColumn(Level level, int x, int z, int minY, int maxY) {
        int y = level.getHeight(MOTION_HEIGTMAP, x, z);
        if (y < minY) y = minY;
        if (y > maxY) y = maxY;

        final BlockPos.MutableBlockPos feet = new BlockPos.MutableBlockPos(x, y, z);
        if (isSafeSpot(level, feet)) return feet.immutable();

        final int vAdj = Math.max(0, 8);
        for (int d = 1; d <= vAdj; d++) {
            int yu = y + d;
            if (yu <= maxY) {
                feet.set(x, yu, z);
                if (isSafeSpot(level, feet)) return feet.immutable();
            }
            int yd = y - d;
            if (yd >= minY) {
                feet.set(x, yd, z);
                if (isSafeSpot(level, feet)) return feet.immutable();
            }
        }
        return null;
    }

    private static boolean isSafeSpot(Level level, BlockPos.MutableBlockPos feet) {
        final BlockPos below = feet.below();
        final BlockState belowState = level.getBlockState(below);

        if (!belowState.isFaceSturdy(level, below, Direction.UP)) return false;

        if (!level.getFluidState(below).isEmpty()) return false;

        if (!isReplaceable(level, feet)) return false;

        feet.move(0, 1, 0);
        boolean headOk = isReplaceable(level, feet.move(0, 1, 0));
        feet.move(0, -1, 0);
        return headOk;
    }

    private static boolean isReplaceable(Level level, BlockPos pos) {
        final BlockState state = level.getBlockState(pos);

        if (state.isAir()) return true;

        final FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) return false;

        if (state.getCollisionShape(level, pos).isEmpty()) return true;

        if (state.is(Blocks.SNOW)) return true;

        return false;
    }
}