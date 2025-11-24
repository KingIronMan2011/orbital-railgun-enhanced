package io.github.kingironman2011.orbital_railgun_enhanced.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.minecraft.util.math.BlockPos;

public class SoundLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("orbital_railgun_enhanced");

    public static void logSoundEvent(String soundName, BlockPos location, double range) {
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("Sound Event: {} | Location: {} | Range: {}", soundName, location, range);
        }
    }
}


