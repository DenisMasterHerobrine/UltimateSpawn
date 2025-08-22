package dev.denismasterherobrine.ultimatespawn.utils;

// Code by TelephaticGhunt, utility method to find valid player spawn location.
// Original: https://github.com/TelepathicGrunt/Bumblezone/blob/250ca9b8e1072bafeb616dc027208ae910cd1cef/src/main/java/com/telepathicgrunt/the_bumblezone/entities/EntityTeleportationBackend.java#L334
// Ported to 1.18.2+

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;

public class ValidSpotChecks {
    public static BlockPos validPlayerSpawnLocation(ServerLevel world, BlockPos position, int maximumRange) {
        // Try to find 2 non-solid spaces around it that the player can spawn at
        int radius;
        int outerRadius;
        int distanceSq;
        BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos(position.getX(), position.getY(), position.getZ());

        // Checks for 2 non-solid blocks with solid block below feet
        // Checks outward from center position in both x, y, and z.
        // The x2, y2, and z2 is so it checks at center of the range box instead of the corner.
        for (int range = 0; range < maximumRange; range++) {
            radius = range * range;
            outerRadius = (range + 1) * (range + 1);

            for (int y = 0; y <= range * 2; y++) {
                int y2 = y > range ? -(y - range) : y;

                for (int x = 0; x <= range * 2; x++) {
                    int x2 = x > range ? -(x - range) : x;

                    for (int z = 0; z <= range * 2; z++) {
                        int z2 = z > range ? -(z - range) : z;

                        distanceSq = x2 * x2 + z2 * z2 + y2 * y2;
                        if (distanceSq >= radius && distanceSq < outerRadius) {
                            currentPos.set(position.offset(x2, y2, z2));

                            if (world.getBlockState(currentPos.below()).canOcclude()
                                    && world.getBlockState(currentPos).isAir()
                                    && world.getBlockState(currentPos.above()).isAir()) {

                                // Valid space for player is found
                                return currentPos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
