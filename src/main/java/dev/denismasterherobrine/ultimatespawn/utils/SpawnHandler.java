package dev.denismasterherobrine.ultimatespawn.utils;

import dev.denismasterherobrine.ultimatespawn.UltimateSpawn;
import dev.denismasterherobrine.ultimatespawn.configuration.Configuration;
import dev.denismasterherobrine.ultimatespawn.utils.teleport.SimpleTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class SpawnHandler {
    private SpawnHandler() {}

    public static void handleSpawn(ServerPlayer player) {
        if (player == null || player.getServer() == null) return;

        final ResourceKey<Level> destKey = parseDimensionKey(Configuration.dimensionEntry.get());
        if (destKey == null) {
            sendError(player, "[§3Ultimate§bSpawn§f] §cBad or empty dimension id: " + Configuration.dimensionEntry.get());
            return;
        }

        final ServerLevel destWorld = player.getServer().getLevel(destKey);
        if (destWorld == null) {
            sendError(player, "[§3Ultimate§bSpawn§f] §cDimension is not loaded or missing: " + destKey.location());
            UltimateSpawn.LOGGER.error("[UltimateSpawn] Destination dimension not found: {}", destKey.location());
            return;
        }

        final Target target = computeTargetPosition(destWorld);
        if (!target.success) {
            sendError(player, target.errorMessage != null ? target.errorMessage : "[§3Ultimate§bSpawn§f] §cFailed to compute target position");
            if (target.errorLog != null) {
                UltimateSpawn.LOGGER.error("[UltimateSpawn] {}", target.errorLog);
            }
            return;
        }

        if (player.level.dimension().equals(destKey)) {
            final double tx = target.pos.getX() + 0.5D;
            final double ty = target.pos.getY() + 0.5D;
            final double tz = target.pos.getZ() + 0.5D;
            final float yaw = player.getYRot();
            final float pitch = player.getXRot();

            destWorld.getChunkSource().addRegionTicket(
                    TicketType.POST_TELEPORT, new ChunkPos(target.pos), 1, player.getId()
            );

            player.teleportTo(tx, ty, tz);
            player.connection.teleport(tx, ty, tz, yaw, pitch);
            player.fallDistance = 0.0F;

            UltimateSpawn.LOGGER.info("Teleported player {} within same dimension {} to {},{},{}",
                    player.getName(), destKey.location(), tx, ty, tz);
            return;
        }

        final double tx = target.pos.getX() + 0.5D;
        final double ty = target.pos.getY() + 0.5D;
        final double tz = target.pos.getZ() + 0.5D;

        destWorld.getChunkSource().addRegionTicket(
                TicketType.POST_TELEPORT, new ChunkPos(target.pos), 1, player.getId()
        );

        final float yaw = player.getYRot();
        final float pitch = player.getXRot();

        player.changeDimension(destWorld, new SimpleTeleporter(tx, ty, tz, yaw, pitch));
        player.fallDistance = 0.0F;

        UltimateSpawn.LOGGER.info("Changed dimension for player {} to {} at {},{},{}",
                player.getName(), destKey.location(), tx, ty, tz);
    }

    @Nullable
    private static ResourceKey<Level> parseDimensionKey(@Nullable String dim) {
        if (dim == null || dim.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(dim);
        if (id == null) return null;
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, id);
    }

    private static Target computeTargetPosition(ServerLevel destWorld) {
        final boolean useCoords = Configuration.useCoordinatesEntry.get();
        final boolean strict = Configuration.strictCoordinatesModeEntry.get();

        if (!useCoords) {
            BlockPos spawn = destWorld.getSharedSpawnPos();
            return Target.ok(spawn);
        }

        double x = Configuration.xEntry.get();
        double y = Configuration.yEntry.get();
        double z = Configuration.zEntry.get();

        double yLowCfg = Configuration.yLowerBoundEntry.get();
        double yHighCfg = Configuration.yUpperBoundEntry.get();

        final int worldMinY = 0;
        final int worldMaxY = destWorld.getMaxBuildHeight() - 1;

        int yLow = (int) Mth.clamp(Math.floor(yLowCfg), worldMinY, worldMaxY);
        int yHigh = (int) Mth.clamp(Math.floor(yHighCfg), worldMinY, worldMaxY);

        if (yLow > yHigh) {
            return Target.fail(
                    "[§3Ultimate§bSpawn§f] §cyLowerBound is greater than yUpperBound, please check your config!",
                    "yLowerBound > yUpperBound in config!; lower=" + yLow + " upper=" + yHigh
            );
        }

        int yClamped = (int) Mth.clamp(Math.floor(y), worldMinY, worldMaxY);

        if (strict) {
            BlockPos pos = new BlockPos(Mth.floor(x), yClamped, Mth.floor(z));
            return Target.ok(pos);
        }

        BlockPos probe = new BlockPos(Mth.floor(x), yClamped, Mth.floor(z));

        int maxRadius = Math.max(0, Math.min(256, Math.abs(yHigh - yLow) + 96));

        BlockPos safe = ValidSpotChecks.findSafeSpawn(destWorld, probe, maxRadius);
        if (safe != null) {
            return Target.ok(safe);
        }

        UltimateSpawn.LOGGER.warn("[UltimateSpawn] Safe spot not found near {},{},{} within radius {}. Fallback to world spawn.",
                probe.getX(), probe.getY(), probe.getZ(), maxRadius);
        return Target.ok(destWorld.getSharedSpawnPos());
    }

    private static void sendError(ServerPlayer player, String msg) {
        player.displayClientMessage(Component.literal(msg), false);
        UltimateSpawn.LOGGER.error("[UltimateSpawn] {}", msg);
    }

    private static final class Target {
        final boolean success;
        final BlockPos pos;
        final String errorMessage;
        final String errorLog;

        private Target(boolean success, BlockPos pos, String errorMessage, String errorLog) {
            this.success = success;
            this.pos = pos;
            this.errorMessage = errorMessage;
            this.errorLog = errorLog;
        }

        static Target ok(BlockPos pos) {
            return new Target(true, pos, null, null);
        }

        static Target fail(String errorMessage, String errorLog) {
            return new Target(false, null, errorMessage, errorLog);
        }
    }
}
