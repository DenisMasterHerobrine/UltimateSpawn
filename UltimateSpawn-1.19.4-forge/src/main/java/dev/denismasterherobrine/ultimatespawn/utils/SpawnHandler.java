package dev.denismasterherobrine.ultimatespawn.utils;

import dev.denismasterherobrine.ultimatespawn.UltimateSpawn;
import dev.denismasterherobrine.ultimatespawn.configuration.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

import static dev.denismasterherobrine.ultimatespawn.utils.ValidSpotChecks.validPlayerSpawnLocation;

public class SpawnHandler {
    public static void handleSpawn(Player player) {
        if (player.getLevel().getServer() == null) return;

        BlockPos location = player.blockPosition();
        ServerLevel world = (ServerLevel) player.getLevel();

        String[] splitted = Configuration.dimensionEntry.get().split(":");

        boolean useCoordinates = Configuration.useCoordinatesEntry.get();
        boolean strictCoordinatesMode = Configuration.strictCoordinatesModeEntry.get();

        if (splitted.length == 2) {
            ResourceKey<Level> destination = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(splitted[0], splitted[1]));

            boolean fatal = false;
            if (world.getServer().getLevel(destination) == null) {
                player.sendSystemMessage(Component.literal("[§3Ultimate§bSpawn§f] §4FATAL ERROR: The dimension " + splitted[0] + ":" + splitted[1] + " does not exist in this modpack instance!"));
                fatal = true;
            }

            if (useCoordinates) {
                if (!fatal) {
                    int x = Configuration.xEntry.get().intValue();
                    int y = Configuration.yEntry.get().intValue();
                    int z = Configuration.zEntry.get().intValue();

                    if (!strictCoordinatesMode) {
                        BlockPos safePos = null;
                        int range = 32;
                        int it = 0;
                        BlockPos searchLocation = new BlockPos(x, y, z);

                        while (safePos == null) {
                            safePos = validPlayerSpawnLocation(world.getServer().getLevel(destination), searchLocation, range);
                            range = range + 16; // If null, adjust the range and search again.
                            if (safePos == null) {
                                if (it > 3) {
                                    UltimateSpawn.LOGGER.info("[UltimateSpawn] WARNING: No safe spots found in specified area in a large area! We're going to shift our safe position and search in the spawn chunks.");
                                    safePos = location;
                                    break;
                                }

                                it++;
                            }
                        }

                        BlockPos finalSafePos = safePos;
                        player.changeDimension(world.getServer().getLevel(destination), new ITeleporter() {
                            @Override
                            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                                entity = repositionEntity.apply(false);
                                entity.teleportTo(finalSafePos.getX(), finalSafePos.getY(), finalSafePos.getZ());
                                return entity;
                            }

                            @Override
                            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                                return false;
                            }
                        });
                    } else {
                        BlockPos finalPos = new BlockPos(x, y, z);
                        player.changeDimension(world.getServer().getLevel(destination), new ITeleporter() {
                            @Override
                            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                                entity = repositionEntity.apply(false);
                                entity.teleportTo(finalPos.getX(), finalPos.getY(), finalPos.getZ());
                                return entity;
                            }

                            @Override
                            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                                return false;
                            }
                        });
                    }
                }
            } else {
                if (!fatal) {
                    BlockPos safePos = null;

                    boolean badConfig = Configuration.yLowerBoundEntry.get() > Configuration.yUpperBoundEntry.get();

                    int y;
                    BlockPos searchLocation = location;
                    if (!badConfig) {
                        y = (int) ((Configuration.yUpperBoundEntry.get() + Configuration.yLowerBoundEntry.get()) / 2); // Optimize the search by starting from center of position
                        searchLocation = new BlockPos(location.getX(), y, location.getZ());
                    }

                    int range = 32;
                    int it = 0;
                    while (safePos == null) {
                        safePos = validPlayerSpawnLocation(world.getServer().getLevel(destination), searchLocation, range);
                        range = range + 16; // If null, adjust the range and search again.
                        if (badConfig) {
                            // We couldn't find anything in the range defined in the config.
                            if (safePos != null) {
                                player.sendSystemMessage(Component.literal("[§3Ultimate§bSpawn§f] §cERROR: Upper and lower bound of Y coordinate form an empty range! The player has been placed in related to initial spawn coordinates in Overworld."));
                                break;
                            }
                        } else {
                            if (safePos != null) {
                                if (it > 3) {
                                    UltimateSpawn.LOGGER.info("[UltimateSpawn] WARNING: No safe spots found in specified area!!! We're going to shift our safe position.");
                                    break;
                                }

                                if (!(Configuration.yLowerBoundEntry.get() < safePos.getY() && safePos.getY() < Configuration.yUpperBoundEntry.get())) {
                                    safePos = null; // We haven't found our position, reset it.
                                    it++;
                                }
                            }
                        }
                    }

                    BlockPos finalSafePos = safePos;
                    player.changeDimension(world.getServer().getLevel(destination), new ITeleporter() {
                        @Override
                        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                            entity = repositionEntity.apply(false);
                            entity.teleportTo(finalSafePos.getX(), finalSafePos.getY(), finalSafePos.getZ());
                            return entity;
                        }

                        @Override
                        public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                            return false;
                        }
                    });
                }
            }
        }
    }
}