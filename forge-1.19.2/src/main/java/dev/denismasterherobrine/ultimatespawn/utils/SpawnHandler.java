package dev.denismasterherobrine.ultimatespawn.utils;

import dev.denismasterherobrine.ultimatespawn.UltimateSpawn;
import dev.denismasterherobrine.ultimatespawn.configuration.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;
import org.jline.utils.Log;

import java.util.function.Function;
import java.util.logging.Logger;

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
            ResourceKey<Level> destination = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(splitted[0], splitted[1]));

            boolean fatal = false;
            if (world.getServer().getLevel(destination) == null) {
                player.sendMessage(new TextComponent("[§3Ultimate§bSpawn§f] §4FATAL ERROR: The dimension " + splitted[0] + ":" + splitted[1] + " does not exist in this modpack instance!"), player.getUUID());
                fatal = true;
            }

            if (useCoordinates) {
                if (!fatal) {
                    double x = Configuration.xEntry.get();
                    double y = Configuration.yEntry.get();
                    double z = Configuration.zEntry.get();

                    if (!strictCoordinatesMode) {
                        BlockPos safePos = null;
                        int range = 32;
                        BlockPos searchLocation = new BlockPos(x, y, z);

                        while (safePos == null) {
                            safePos = validPlayerSpawnLocation(world.getServer().getLevel(destination), searchLocation, range);
                            range = range + 16; // If null, adjust the range and search again.
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

                    double y;
                    BlockPos searchLocation = location;
                    if (!badConfig) {
                        y = (Configuration.yUpperBoundEntry.get() + Configuration.yLowerBoundEntry.get()) / 2; // Optimize the search by starting from center of position
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
                                player.sendMessage(new TextComponent("[§3Ultimate§bSpawn§f] §cERROR: Upper and lower bound of Y coordinate form an empty range! The player has been placed in related to initial spawn coordinates in Overworld."), player.getUUID());
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


//package dev.denismasterherobrine.ultimatespawn.utils;
//

//
//import java.util.function.Function;
//
//import static dev.denismasterherobrine.ultimatespawn.utils.ValidSpotChecks.validPlayerSpawnLocation;
//
//public class SpawnHandler {
//    public static void handleSpawn(Player player) {
//        BlockPos location = player.blockPosition();
//        ServerLevel world = (ServerLevel) player.getLevel();
//
//        String dimension = Configuration.dimensionEntry.get();
//        String[] splitted = dimension.split(":");
//
//        boolean useCoordinates = Configuration.useCoordinatesEntry.get();
//
//        if (splitted.length == 2) {
//            ResourceKey<Level> destination = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(splitted[0], splitted[1]));
////
////            System.out.println(location);
////            System.out.println(destination);
////            System.out.println(player.level.getServer());
////            System.out.println(player.getLevel().dimension());
////            System.out.println(player.level.getServer().getLevel(destination));
//
//
//            if (player.level.getServer() == null) return;
//
//            boolean fatal = false;
//            if (player.level.getServer().getLevel(destination) == null) {
//                player.sendMessage(new TextComponent("[§3Ultimate§bSpawn§f] §4FATAL ERROR: The dimension " + splitted[0] + ":" + splitted[1] + " does not exist in this modpack instance!"), player.getUUID());
//                fatal = true;
//            }
//
//            if (useCoordinates) {
//                if (!fatal) {
//                    double x = Configuration.xEntry.get();
//                    double y = Configuration.yEntry.get();
//                    double z = Configuration.zEntry.get();
//
//                    player.changeDimension(world.getLevel().getServer().getLevel(destination), new ITeleporter() {
//                        @Override
//                        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
//                            entity = repositionEntity.apply(false);
//                            entity.teleportTo(x, y, z);
//                            return entity;
//                        }
//
//                        @Override
//                        public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld)
//                        {
//                            return false;
//                        }
//                    });
//                }
//            }
//            else {
//                if (!fatal) {
//                    BlockPos safePos = null;
//
//                    boolean badConfig = Configuration.yLowerBoundEntry.get() > Configuration.yUpperBoundEntry.get();
//
//                    double y;
//                    BlockPos searchLocation = location;
//                    if (!badConfig) {
//                        y = (Configuration.yUpperBoundEntry.get() + Configuration.yLowerBoundEntry.get()) / 2; // Optimize the search by starting from center of position
//                        searchLocation = new BlockPos(location.getX(), y, location.getZ());
//                    }
//
//                    int range = 32;
//                    while (safePos == null) {
//                        safePos = validPlayerSpawnLocation(player.level.getServer().getLevel(destination), searchLocation, range);
//                        range = range + 16; // If null, adjust the range and search again.
//
//                        if (badConfig) {
//                            // We couldn't find anything in the range defined in the config.
//                            if (safePos != null) {
//                                player.sendMessage(new TextComponent("[§3Ultimate§bSpawn§f] §cERROR: Upper and lower bound of Y coordinate form an empty range! The player has been placed in related to initial spawn coordinates in Overworld."), player.getUUID());
//                                break;
//                            }
//                        } else {
//                            if (safePos != null) {
//                                if (!(Configuration.yLowerBoundEntry.get() < safePos.getY() && safePos.getY() < Configuration.yUpperBoundEntry.get())) {
//                                    safePos = null; // We haven't found our position, reset it.
//                                }
//                            }
//                        }
//                    }
//
//
//                    BlockPos finalSafePos = safePos;
//                    player.changeDimension(player.level.getServer().getLevel(destination), new ITeleporter() {
//                        @Override
//                        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
//                            entity = repositionEntity.apply(false);
//                            entity.teleportTo(finalSafePos.getX(), finalSafePos.getY(), finalSafePos.getZ());
//                            return entity;
//                        }
//
//                        @Override
//                        public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld)
//                        {
//                            return false;
//                        }
//                    });
//                }}
//        }
//    }
//}
