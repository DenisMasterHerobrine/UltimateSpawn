package dev.denismasterherobrine.ultimatespawn.utils.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
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
    public Entity placeEntity(Entity entity, ServerWorld current, ServerWorld dest, float yawIgnored, Function<Boolean, Entity> reposition) {
        Entity e = reposition.apply(false);

        e.teleportTo(x, y, z);

        if (e instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) e).connection.teleport(x, y, z, this.yaw, this.pitch);
            e.fallDistance = 0.0F;
        }

        return e;
    }

    @Override
    public boolean playTeleportSound(ServerPlayerEntity player, ServerWorld source, ServerWorld target) {
        return false;
    }
}
