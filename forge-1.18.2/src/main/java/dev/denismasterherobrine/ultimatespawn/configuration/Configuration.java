package dev.denismasterherobrine.ultimatespawn.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class Configuration {
    public static ForgeConfigSpec.ConfigValue<String> dimensionEntry;

    public static ForgeConfigSpec.BooleanValue useCoordinatesEntry;
    public static ForgeConfigSpec.BooleanValue strictCoordinatesModeEntry;

    public static ForgeConfigSpec.DoubleValue xEntry;
    public static ForgeConfigSpec.DoubleValue yEntry;
    public static ForgeConfigSpec.DoubleValue zEntry;

    public static ForgeConfigSpec.DoubleValue yUpperBoundEntry;

    public static ForgeConfigSpec.DoubleValue yLowerBoundEntry;

    public static ForgeConfigSpec config;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        config = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.comment(" Welcome to the UltimateSpawn config.\n There you can define a preffered dimension and coordinates for player spawning. \n It is **preffered** to reload the game after changes,\n but it **may** work just fine each config saving to disk and adapt in real time.");
        builder.push("General");

        dimensionEntry = builder
                .comment(" There you can define a default dimension to spawn. Syntax: modid:world_id. You can use '/execute in' tab-completion to find a list of available dimensions.")
                .define("dimension", "minecraft:overworld");

        useCoordinatesEntry = builder
                .comment(" There you can define if UltimateSpawn should spawn players in specific coordinates or not. If true, then UltimateSpawn will try to spawn in place with specified x, y, z config options, if false - UltimateSpawn will try to find the safe spot to spawn players in the spawn chunks or near them.")
                .define("useCoordinates", false);

        strictCoordinatesModeEntry = builder
                .comment(" There you can define if UltimateSpawn should check whether there is a safe position to spawn a player on specified coordinates in X, Y and Z config entries. If true, then UltimateSpawn will strictly respect specified coordinates and try to spawn a player even if there are blocks/unsafe stuff such as lava and etc.., if false - UltimateSpawn will try to find the safe spot to spawn players close to the specified coordinates.")
                .define("strictCoordinatesMode", false);

        yUpperBoundEntry = builder
                .comment(" There you can define if UltimateSpawn should spawn players below this Y level. In rare cases may be shifted to speed up the search of the safe spawn spot. If this value is lower than lower bound, then it will be ignored and throw an error in chat.")
                .defineInRange("yUpperBound", 140, -64D, 320D);

        yLowerBoundEntry = builder
                .comment(" There you can define if UltimateSpawn should spawn players above this Y level. In rare cases may be shifted to speed up the search of the safe spawn spot. If this value is higher than higher bound, then it will be ignored and throw an error in chat.")
                .defineInRange("yLowerBound", 60, -64D, 320D);

        xEntry = builder.defineInRange("x", 0D, Double.MIN_VALUE, Double.MAX_VALUE);
        yEntry = builder.defineInRange("y", 100D, Double.MIN_VALUE, Double.MAX_VALUE);
        zEntry = builder.defineInRange("z", 0D, Double.MIN_VALUE, Double.MAX_VALUE);

        builder.pop();
    }
}
