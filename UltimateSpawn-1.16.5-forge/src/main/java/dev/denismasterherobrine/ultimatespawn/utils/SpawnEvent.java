package dev.denismasterherobrine.ultimatespawn.utils;

import dev.denismasterherobrine.ultimatespawn.UltimateSpawn;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimateSpawn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnEvent {
    @SubscribeEvent
    public static void onPlayerFirstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() != null) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

            int statCounter = player.getStats().getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME));

            if (statCounter == 0) {
                SpawnHandler.handleSpawn(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() != null) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

            if (player.getRespawnPosition() == null) {
                SpawnHandler.handleSpawn(player);
            }
        }
    }
}
