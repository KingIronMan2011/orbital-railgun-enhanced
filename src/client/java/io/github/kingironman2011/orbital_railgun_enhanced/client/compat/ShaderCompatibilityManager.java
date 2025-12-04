package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import ladysnake.satin.api.event.PostWorldRenderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main API for shader compatibility management.
 * This manager handles switching between Satin shaders and Iris-compatible fallbacks
 * to prevent crashes and visual glitches when Iris is installed.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Initialize during client mod init
 * ShaderCompatibilityManager.getInstance().initialize();
 *
 * // Register with event callbacks
 * ClientTickEvents.END_CLIENT_TICK.register(ShaderCompatibilityManager.getInstance());
 * PostWorldRenderCallback.EVENT.register(ShaderCompatibilityManager.getInstance());
 *
 * // Trigger a strike effect
 * ShaderCompatibilityManager.getInstance().triggerStrike(blockPos, dimension);
 *
 * // Stop an active animation
 * ShaderCompatibilityManager.getInstance().stopAnimation();
 * }</pre>
 *
 * <h2>Compatibility Mode</h2>
 * When Iris is detected with an active shader pack, the manager automatically
 * switches from Satin post-processing shaders to a particle-based fallback.
 * This prevents crashes caused by conflicting shader pipelines.
 *
 * @since 1.4.0
 */
public final class ShaderCompatibilityManager
        implements PostWorldRenderCallback, ClientTickEvents.EndTick {

    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    private static final ShaderCompatibilityManager INSTANCE = new ShaderCompatibilityManager();

    private final SatinShaderProvider satinProvider;
    private final IrisFallbackProvider fallbackProvider;
    private ShaderProvider activeProvider;
    private boolean initialized = false;

    // Check interval for Iris shader pack status (every 100 ticks = 5 seconds)
    private static final int IRIS_CHECK_INTERVAL = 100;
    private int ticksSinceLastCheck = 0;

    private ShaderCompatibilityManager() {
        this.satinProvider = new SatinShaderProvider();
        this.fallbackProvider = new IrisFallbackProvider();
        this.activeProvider = null;
    }

    /**
     * Gets the singleton instance of the shader compatibility manager.
     *
     * @return the manager instance
     */
    public static ShaderCompatibilityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the shader compatibility manager.
     * This should be called during client mod initialization.
     */
    public void initialize() {
        if (initialized) {
            LOGGER.warn("ShaderCompatibilityManager already initialized");
            return;
        }

        LOGGER.info("Initializing Shader Compatibility Manager...");
        LOGGER.info("Iris detection status: {}", IrisCompatibilityHelper.getStatusString());

        // Initialize both providers
        satinProvider.initialize();
        fallbackProvider.initialize();

        // Select initial provider based on Iris status
        selectActiveProvider();

        initialized = true;
        LOGGER.info("Shader Compatibility Manager initialized with {} provider",
                activeProvider.getProviderName());
    }

    /**
     * Selects the appropriate shader provider based on current Iris status.
     */
    private void selectActiveProvider() {
        ShaderProvider previousProvider = activeProvider;

        if (IrisCompatibilityHelper.shouldDisableSatinShaders()) {
            activeProvider = fallbackProvider;
        } else {
            activeProvider = satinProvider;
        }

        // If provider changed, transfer state
        if (previousProvider != null && previousProvider != activeProvider) {
            LOGGER.info("Switching shader provider from {} to {}",
                    previousProvider.getProviderName(), activeProvider.getProviderName());

            Vector3f pos = previousProvider.getBlockPosition();
            if (pos != null) {
                activeProvider.setBlockPosition(pos);
            }
        }
    }

    /**
     * Triggers an orbital strike visual effect at the specified position.
     *
     * @param blockPosition the center position of the strike
     * @param dimension the dimension where the strike occurs
     */
    public void triggerStrike(Vector3f blockPosition, RegistryKey<World> dimension) {
        if (!initialized) {
            LOGGER.warn("Cannot trigger strike - manager not initialized");
            return;
        }

        LOGGER.debug("Triggering strike at {} in {}", blockPosition, dimension);

        // Set the position on both providers (for seamless switching)
        satinProvider.setBlockPosition(blockPosition);
        satinProvider.setDimension(dimension);

        fallbackProvider.setBlockPosition(blockPosition);
        fallbackProvider.setDimension(dimension);
    }

    /**
     * Stops any active strike animation.
     */
    public void stopAnimation() {
        if (!initialized) {
            return;
        }

        satinProvider.stopAnimation();
        fallbackProvider.stopAnimation();
        LOGGER.debug("Strike animation stopped");
    }

    /**
     * Checks if Satin shaders are currently in use.
     *
     * @return true if using Satin shaders
     */
    public boolean isUsingSatinShaders() {
        return activeProvider == satinProvider;
    }

    /**
     * Checks if the fallback provider is currently active.
     *
     * @return true if using fallback provider
     */
    public boolean isUsingFallback() {
        return activeProvider == fallbackProvider;
    }

    /**
     * Gets the name of the currently active shader provider.
     *
     * @return provider name
     */
    public String getActiveProviderName() {
        return activeProvider != null ? activeProvider.getProviderName() : "None";
    }

    /**
     * Forces a re-check of Iris status and potentially switches providers.
     * Useful after Iris shader pack changes.
     */
    public void refreshIrisStatus() {
        IrisCompatibilityHelper.resetCache();
        selectActiveProvider();
    }

    /**
     * Gets the current Iris compatibility status string.
     *
     * @return status description
     */
    public String getCompatibilityStatus() {
        return IrisCompatibilityHelper.getStatusString();
    }

    /**
     * Provides direct access to the Satin provider for legacy compatibility.
     *
     * @return the Satin shader provider
     */
    public SatinShaderProvider getSatinProvider() {
        return satinProvider;
    }

    // Event handlers

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (!initialized || activeProvider == null) {
            return;
        }

        if (activeProvider.shouldRender()) {
            activeProvider.render(camera, tickDelta, nanoTime);
        }
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (!initialized || activeProvider == null) {
            return;
        }

        // Check Iris status every tick for responsive switching
        ticksSinceLastCheck++;
        if (ticksSinceLastCheck >= IRIS_CHECK_INTERVAL) {
            ticksSinceLastCheck = 0;
            IrisCompatibilityHelper.resetCache();
        }

        // Always check and potentially switch providers
        selectActiveProvider();

        // Tick both providers to keep state in sync
        satinProvider.onTick();
        fallbackProvider.onTick();
    }

    /**
     * Disposes of resources when the manager is no longer needed.
     */
    public void dispose() {
        satinProvider.dispose();
        fallbackProvider.dispose();
        initialized = false;
    }
}
