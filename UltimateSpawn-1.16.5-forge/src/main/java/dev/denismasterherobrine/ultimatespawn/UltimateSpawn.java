package dev.denismasterherobrine.ultimatespawn;

import dev.denismasterherobrine.ultimatespawn.configuration.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("ultimatespawn")
public class UltimateSpawn {
    public static final String MOD_ID = "ultimatespawn";
    public static final Logger LOGGER = LogManager.getLogger();

    public UltimateSpawn() {
        // Register the setup method for modloading
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.config, "UltimateSpawn.toml");

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }
}
