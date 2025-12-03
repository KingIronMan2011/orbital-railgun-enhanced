package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import ladysnake.satin.api.event.PostWorldRenderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.util.hit.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for GUI shader compatibility with Iris.
 * Handles the targeting overlay shader when aiming the orbital railgun.
 *
 * @since 1.4.0
 */
public final class GuiShaderCompatibilityManager
        implements PostWorldRenderCallback, ClientTickEvents.EndTick {

    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    private static final GuiShaderCompatibilityManager INSTANCE = new GuiShaderCompatibilityManager();

    private final SatinGuiShaderProvider satinProvider;
    private final IrisGuiFallbackProvider fallbackProvider;
    private ShaderProvider activeProvider;
    private boolean initialized = false;

    // Check interval for Iris shader pack status
    private static final int IRIS_CHECK_INTERVAL = 100;
    private int ticksSinceLastCheck = 0;

    private GuiShaderCompatibilityManager() {
        this.satinProvider = new SatinGuiShaderProvider();
        this.fallbackProvider = new IrisGuiFallbackProvider();
        this.activeProvider = null;
    }

    /**
     * Gets the singleton instance.
     *
     * @return the manager instance
     */
    public static GuiShaderCompatibilityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the GUI shader compatibility manager.
     */
    public void initialize() {
        if (initialized) {
            LOGGER.warn("GuiShaderCompatibilityManager already initialized");
            return;
        }

        LOGGER.info("Initializing GUI Shader Compatibility Manager...");

        satinProvider.initialize();
        fallbackProvider.initialize();

        selectActiveProvider();

        initialized = true;
        LOGGER.info("GUI Shader Compatibility Manager initialized with {} provider",
                activeProvider.getProviderName());
    }

    private void selectActiveProvider() {
        ShaderProvider previousProvider = activeProvider;

        if (IrisCompatibilityHelper.shouldDisableSatinShaders()) {
            activeProvider = fallbackProvider;
        } else {
            activeProvider = satinProvider;
        }

        if (previousProvider != null && previousProvider != activeProvider) {
            LOGGER.info("Switching GUI shader provider from {} to {}",
                    previousProvider.getProviderName(), activeProvider.getProviderName());
        }
    }

    /**
     * Checks if Satin GUI shaders are currently in use.
     *
     * @return true if using Satin shaders
     */
    public boolean isUsingSatinShaders() {
        return activeProvider == satinProvider;
    }

    /**
     * Gets the hit result from the active provider.
     *
     * @return the current hit result, or null
     */
    public HitResult getHitResult() {
        if (activeProvider == satinProvider) {
            return satinProvider.getHitResult();
        } else if (activeProvider == fallbackProvider) {
            return fallbackProvider.getHitResult();
        }
        return null;
    }

    /**
     * Renders the fallback HUD if the fallback provider is active.
     * This should be called from a HUD render event.
     *
     * @param context the draw context
     * @param tickDelta the partial tick time
     */
    public void renderFallbackHud(DrawContext context, float tickDelta) {
        if (!initialized || activeProvider != fallbackProvider) {
            return;
        }

        fallbackProvider.renderHud(context, tickDelta);
    }

    /**
     * Checks if the fallback HUD should be rendered.
     *
     * @return true if fallback HUD rendering is needed
     */
    public boolean shouldRenderFallbackHud() {
        return initialized && activeProvider == fallbackProvider && fallbackProvider.shouldRender();
    }

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

        ticksSinceLastCheck++;
        if (ticksSinceLastCheck >= IRIS_CHECK_INTERVAL) {
            ticksSinceLastCheck = 0;
            IrisCompatibilityHelper.resetCache();
            selectActiveProvider();
        }

        satinProvider.onTick();
        fallbackProvider.onTick();
    }

    /**
     * Disposes of resources.
     */
    public void dispose() {
        satinProvider.dispose();
        fallbackProvider.dispose();
        initialized = false;
    }
}
