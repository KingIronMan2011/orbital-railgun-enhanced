package io.github.kingironman2011.orbital_railgun_enhanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItem;
import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItems;
import io.github.kingironman2011.orbital_railgun_enhanced.util.OrbitalRailgunStrikeManager;
import io.github.kingironman2011.orbital_railgun_enhanced.registry.SoundsRegistry;
import io.github.kingironman2011.orbital_railgun_enhanced.registry.CommandRegistry;
import io.github.kingironman2011.orbital_railgun_enhanced.logger.SoundLogger;
import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import io.github.kingironman2011.orbital_railgun_enhanced.listener.PlayerAreaListener;
import io.github.kingironman2011.orbital_railgun_enhanced.network.ClientSyncPayload;
import io.github.kingironman2011.orbital_railgun_enhanced.network.PlaySoundPayload;
import io.github.kingironman2011.orbital_railgun_enhanced.network.ShootPayload;
import io.github.kingironman2011.orbital_railgun_enhanced.network.StopAreaSoundPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;

import java.util.List;

public class OrbitalRailgun implements ModInitializer {
    public static final String MOD_ID = "orbital_railgun_enhanced";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final long RAILGUN_SOUND_DURATION_MS = 52992L;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Orbital Railgun Enhanced...");

        ServerConfig.INSTANCE.loadConfig();
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("[DEBUG] Debug mode is enabled");
        }

        SoundsRegistry.initialize();
        LOGGER.info("Sounds registry initialized");

        CommandRegistry.registerCommands();
        LOGGER.info("Commands registered: /ore and /orbitalrailgun");

        OrbitalRailgunItems.initialize();
        LOGGER.info("Items registered");

        OrbitalRailgunStrikeManager.initialize();
        LOGGER.info("Strike manager initialized");

        PlayerAreaListener.setAreaChangeCallback(
                event ->
                        handleAreaStateChange(event.player(), event.result(), event.laserX(), event.laserZ()));

        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("Registered player area change callback");
        }

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {
                    PlayerAreaListener.clearPlayerState(handler.getPlayer().getUuid());
                    if (ServerConfig.INSTANCE.isDebugMode()) {
                        LOGGER.info(
                                "[NETWORK] Cleared area state for disconnected player: {}",
                                handler.getPlayer().getName().getString());
                    }
                });

        // Register payload types for networking
        PayloadTypeRegistry.playC2S().register(PlaySoundPayload.ID, PlaySoundPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ShootPayload.ID, ShootPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientSyncPayload.ID, ClientSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopAreaSoundPayload.ID, StopAreaSoundPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                PlaySoundPayload.ID,
                (payload, context) -> {
                    Identifier soundId = payload.soundId();
                    SoundEvent sound = Registries.SOUND_EVENT.get(soundId);
                    BlockPos blockPos = payload.blockPos();
                    float volumeShoot = payload.volume();
                    float pitchShoot = payload.pitch();
                    ServerPlayerEntity player = context.player();

                    long fireTimestamp = System.currentTimeMillis();

                    context.server().execute(
                            () -> {
                                if (sound == null) {
                                    OrbitalRailgun.LOGGER.warn(
                                            "[NETWORK] Received unknown sound id: {}", soundId.toString());
                                    return;
                                }

                                double range = ServerConfig.INSTANCE.getSoundRange();
                                double rangeSquared = range * range;
                                double laserX = blockPos.getX() + 0.5;
                                double laserZ = blockPos.getZ() + 0.5;

                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    LOGGER.info(
                                            "[NETWORK] Received PLAY_SOUND_PACKET from player: {}",
                                            player.getName().getString());
                                    LOGGER.info(
                                            "[NETWORK] Playing sound {} at BlockPos: {} with range: {} at time {}",
                                            soundId,
                                            blockPos,
                                            range,
                                            fireTimestamp);
                                }

                                // Check all players and track state changes
                                context.server()
                                        .getPlayerManager()
                                        .getPlayerList()
                                        .forEach(
                                                nearbyPlayer -> {
                                                    double distanceSquared =
                                                            nearbyPlayer.squaredDistanceTo(
                                                                    blockPos.getX() + 0.5,
                                                                    blockPos.getY() + 0.5,
                                                                    blockPos.getZ() + 0.5);

                                                    PlayerAreaListener.AreaCheckResult result =
                                                            PlayerAreaListener.handlePlayerAreaCheck(
                                                                    nearbyPlayer, laserX, laserZ, fireTimestamp);
                                                    if (distanceSquared <= rangeSquared) {
                                                        // Use PlayerAreaListener to track state changes with timestamp

                                                        if (result.isInside) {
                                                            // Only play sound if player is in range
                                                            nearbyPlayer.playSoundToPlayer(
                                                                    sound, SoundCategory.PLAYERS, volumeShoot, pitchShoot);
                                                            SoundLogger.logSoundEvent(soundId.toString(), blockPos, range);
                                                            SoundLogger.logSoundPlayed(
                                                                    nearbyPlayer.getName().getString(),
                                                                    soundId.toString(),
                                                                    volumeShoot,
                                                                    pitchShoot);

                                                            if (ServerConfig.INSTANCE.isDebugMode()) {
                                                                LOGGER.info(
                                                                        "[SOUND] Playing sound to player {} (distance: {})",
                                                                        nearbyPlayer.getName().getString(),
                                                                        Math.sqrt(distanceSquared));
                                                            }
                                                        }

                                                        // Handle state changes (enter/leave detection)
                                                        handleAreaStateChange(nearbyPlayer, result, laserX, laserZ);
                                                    } else {
                                                        // Player is outside range - check if they left the zone

                                                        if (result.hasLeft()) {
                                                            // Player just left the range
                                                            handleAreaStateChange(nearbyPlayer, result, laserX, laserZ);
                                                        }
                                                    }
                                                });
                            });
                });

        ServerPlayNetworking.registerGlobalReceiver(
                ShootPayload.ID,
                (payload, context) -> {
                    OrbitalRailgunItem orbitalRailgun = (OrbitalRailgunItem) payload.itemStack().getItem();
                    BlockPos blockPos = payload.blockPos();
                    ServerPlayerEntity player = context.player();

                    if (ServerConfig.INSTANCE.isDebugMode()) {
                        LOGGER.info("========================================");
                        LOGGER.info(
                                "[NETWORK] SHOOT_PACKET received from player: {}", player.getName().getString());
                        LOGGER.info("[STRIKE] Impact location: {}", blockPos);
                    }

                    context.server().execute(
                            () -> {
                                double laserX = blockPos.getX() + 0.5;
                                double laserZ = blockPos.getZ() + 0.5;

                                orbitalRailgun.shoot(player);

                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    LOGGER.info("[STRIKE] Orbital railgun fired at ({}, {})", laserX, laserZ);
                                }

                                List<Entity> nearby =
                                        player
                                                .getWorld()
                                                .getOtherEntities(null, Box.of(blockPos.toCenterPos(), 500., 500., 500.));
                                OrbitalRailgunStrikeManager.activeStrikes.put(
                                        new Pair<>(blockPos, nearby),
                                        new Pair<>(context.server().getTicks(), player.getWorld().getRegistryKey()));

                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    LOGGER.info("[STRIKE] Registered strike with {} nearby entities", nearby.size());
                                }

                                nearby.forEach(
                                        (entity -> {
                                            if (entity instanceof ServerPlayerEntity serverPlayer) {
                                                ServerPlayNetworking.send(
                                                        serverPlayer,
                                                        new ClientSyncPayload(blockPos));
                                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                                    LOGGER.debug(
                                                            "[NETWORK] Sent CLIENT_SYNC_PACKET to {}",
                                                            serverPlayer.getName().getString());
                                                }
                                            }
                                        }));

                                int totalPlayers = context.server().getPlayerManager().getPlayerList().size();
                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    LOGGER.info("[STRIKE] Checking {} players on server for range", totalPlayers);
                                }

                                context.server()
                                        .getPlayerManager()
                                        .getPlayerList()
                                        .forEach(
                                                serverPlayer -> {
                                                    PlayerAreaListener.AreaCheckResult result =
                                                            PlayerAreaListener.handlePlayerAreaCheck(
                                                                    serverPlayer, laserX, laserZ);

                                                    handleAreaStateChange(serverPlayer, result, laserX, laserZ);
                                                });

                                if (ServerConfig.INSTANCE.isDebugMode()) {
                                    LOGGER.info("========================================");
                                }
                            });
                });

        ServerTickEvents.END_SERVER_TICK.register(
                server -> {
                    if (server.getTicks() % 20 == 0) {
                        server
                                .getPlayerManager()
                                .getPlayerList()
                                .forEach(PlayerAreaListener::checkPlayerPosition);
                    }
                });

        ServerTickEvents.END_SERVER_TICK.register(OrbitalRailgunStrikeManager::tick);

        LOGGER.info("Orbital Railgun Enhanced initialization complete!");
    }

    /**
     * Handles area state changes for a player (entering/leaving the sound range). Plays railgun
     * sounds to players who are in range when the railgun fires.
     */
    private static void handleAreaStateChange(
            ServerPlayerEntity player,
            PlayerAreaListener.AreaCheckResult result,
            double laserX,
            double laserZ) {
        if (result.hasEntered()) {
            // Player just entered the sound range
            long currentTime = System.currentTimeMillis();
            long elapsedMs = currentTime - result.fireTimestamp;

            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info(
                        "[AREA] Player {} entered sound range at ({}, {}) - elapsed: {}ms, duration: {}ms",
                        player.getName().getString(),
                        laserX,
                        laserZ,
                        elapsedMs,
                        RAILGUN_SOUND_DURATION_MS);
            }

            SoundLogger.logPlayerEnterRange(
                    player.getName().getString(),
                    Math.sqrt(player.squaredDistanceTo(laserX, player.getY(), laserZ)));

            // Only play sound if it hasn't finished yet
            if (elapsedMs < RAILGUN_SOUND_DURATION_MS) {
                // Play the railgun shoot sound to the player who just entered range
                playRailgunSoundToPlayer(player, laserX, laserZ, elapsedMs);
            } else {
                if (ServerConfig.INSTANCE.isDebugMode()) {
                    LOGGER.info(
                            "[AREA] Sound already ended ({}ms > {}ms) - not playing for player {}",
                            elapsedMs,
                            RAILGUN_SOUND_DURATION_MS,
                            player.getName().getString());
                }
            }

        } else if (result.hasLeft()) {
            // Player just left the sound range - stop any playing area sounds
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info(
                        "[AREA] Player {} left sound range at ({}, {}) - stopping sounds",
                        player.getName().getString(),
                        laserX,
                        laserZ);
            }

            SoundLogger.logPlayerExitRange(player.getName().getString());

            // Send packet to client to stop area-based sounds
            stopAreaSoundsForPlayer(player);

        } else if (result.isInside) {
            // Player is still inside the range (already heard the sound)
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.debug(
                        "[AREA] Player {} remains in sound range at ({}, {})",
                        player.getName().getString(),
                        laserX,
                        laserZ);
            }
        }
    }

    /**
     * Plays the railgun shoot sound to a specific player at the laser impact location.
     *
     * @param elapsedMs How many milliseconds have elapsed since the sound started (for syncing)
     */
    private static void playRailgunSoundToPlayer(
            ServerPlayerEntity player, double laserX, double laserZ, long elapsedMs) {
        // Use the railgun shoot sound from the registry
        SoundEvent shootSound = SoundsRegistry.RAILGUN_SHOOT;

        if (shootSound != null) {
            // Play the sound at the laser impact location
            player.playSoundToPlayer(
                    shootSound,
                    SoundCategory.PLAYERS,
                    1.0f, // volume
                    1.0f // pitch
            );

            SoundLogger.logSoundPlayed(
                    player.getName().getString(), SoundsRegistry.RAILGUN_SHOOT_ID.toString(), 1.0f, 1.0f);

            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info(
                        "[SOUND] Playing railgun shoot sound to player {} at ({}, {}) with {}ms offset",
                        player.getName().getString(),
                        laserX,
                        laserZ,
                        elapsedMs);
            }
        } else {
            LOGGER.warn("[SOUND] Railgun shoot sound not found in registry");
        }
    }

    /**
     * Sends a packet to the client to stop area-based sounds.
     */
    private static void stopAreaSoundsForPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new StopAreaSoundPayload(SoundsRegistry.RAILGUN_SHOOT_ID));

        SoundLogger.logSoundStopped(
                player.getName().getString(), SoundsRegistry.RAILGUN_SHOOT_ID.toString());

        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("[NETWORK] Sent stop sound packet to player {}", player.getName().getString());
        }
    }
}
