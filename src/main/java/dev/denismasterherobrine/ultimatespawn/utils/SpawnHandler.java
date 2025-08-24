package dev.denismasterherobrine.ultimatespawn.utils;

import dev.denismasterherobrine.ultimatespawn.UltimateSpawn;
import dev.denismasterherobrine.ultimatespawn.configuration.Configuration;
import dev.denismasterherobrine.ultimatespawn.utils.teleport.SimpleTeleporter;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

import javax.annotation.Nullable;

public final class SpawnHandler {
    private SpawnHandler() {}

    public static void handleSpawn(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) return;

        final RegistryKey<World> destKey = parseDimensionKey(Configuration.dimensionEntry.get());
        if (destKey == null) {
            sendError(player, "[§3Ultimate§bSpawn§f] §cBad or empty dimension id: " + Configuration.dimensionEntry.get());
            return;
        }

        final ServerWorld destWorld = player.getServer().getLevel(destKey);
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

            destWorld.getChunkSource().addRegionTicket(
                    TicketType.POST_TELEPORT, new ChunkPos(target.pos), 1, player.getId()
            );

            player.teleportTo(tx, ty, tz);
            player.connection.teleport(tx, ty, tz, player.yRot, player.xRot);
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

        final float yaw = player.yRot;
        final float pitch = player.xRot;

        player.changeDimension(destWorld, new SimpleTeleporter(tx, ty, tz, yaw, pitch));
        player.fallDistance = 0.0F;

        UltimateSpawn.LOGGER.info("Changed dimension for player {} to {} at {},{},{}",
                player.getName(), destKey.location(), tx, ty, tz);
    }

    @Nullable
    private static RegistryKey<World> parseDimensionKey(@Nullable String dim) {
        if (dim == null || dim.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(dim);
        if (id == null) return null;
        return RegistryKey.create(Registry.DIMENSION_REGISTRY, id);
    }

    private static Target computeTargetPosition(ServerWorld destWorld) {
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

        int yLow = (int) MathHelper.clamp(Math.floor(yLowCfg), worldMinY, worldMaxY);
        int yHigh = (int) MathHelper.clamp(Math.floor(yHighCfg), worldMinY, worldMaxY);

        if (yLow > yHigh) {
            return Target.fail(
                    "[§3Ultimate§bSpawn§f] §cyLowerBound is greater than yUpperBound, please check your config!",
                    "yLowerBound > yUpperBound in config!; lower=" + yLow + " upper=" + yHigh
            );
        }

        int yClamped = (int) MathHelper.clamp(Math.floor(y), worldMinY, worldMaxY);

        if (strict) {
            BlockPos pos = new BlockPos(MathHelper.floor(x), yClamped, MathHelper.floor(z));
            return Target.ok(pos);
        }

        BlockPos probe = new BlockPos(MathHelper.floor(x), yClamped, MathHelper.floor(z));

        int maxRadius = Math.max(0, Math.min(256, Math.abs(yHigh - yLow) + 96));

        BlockPos safe = ValidSpotChecks.findSafeSpawn(destWorld, probe, maxRadius);
        if (safe != null) {
            return Target.ok(safe);
        }

        UltimateSpawn.LOGGER.warn("[UltimateSpawn] Safe spot not found near {},{},{} within radius {}. Fallback to world spawn.",
                probe.getX(), probe.getY(), probe.getZ(), maxRadius);
        return Target.ok(destWorld.getSharedSpawnPos());
    }

    private static void sendError(ServerPlayerEntity player, String msg) {
        player.displayClientMessage(new StringTextComponent(msg), false);
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
