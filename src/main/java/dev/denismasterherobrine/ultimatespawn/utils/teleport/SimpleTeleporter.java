package dev.denismasterherobrine.ultimatespawn.utils.teleport;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class SimpleTeleporter implements ITeleporter {
    private final double x, y, z;
    private final float yaw, pitch;

    public SimpleTeleporter(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel current, ServerLevel dest, float yawIgnored, Function<Boolean, Entity> reposition) {
        Entity e = reposition.apply(false);

        e.teleportTo(x, y, z);

        if (e instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.teleport(x, y, z, this.yaw, this.pitch);
            serverPlayer.fallDistance = 0.0F;
        }
        return e;
    }

    @Override
    public boolean playTeleportSound(ServerPlayer player, ServerLevel source, ServerLevel target) {
        return false;
    }
}
