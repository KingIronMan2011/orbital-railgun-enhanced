package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import net.minecraft.client.render.Camera;
import org.joml.Vector3f;

/**
 * Interface for shader rendering providers.
 * Implementations can provide either Satin-based or fallback rendering.
 */
public interface ShaderProvider {

    /**
     * Initializes the shader provider.
     * Called once during mod initialization.
     */
    void initialize();

    /**
     * Checks if this provider is available and can render.
     *
     * @return true if the provider can render shaders
     */
    boolean isAvailable();

    /**
     * Checks if rendering should be active this frame.
     *
     * @return true if shaders should be rendered
     */
    boolean shouldRender();

    /**
     * Called when the shader should render.
     *
     * @param camera the current camera
     * @param tickDelta the partial tick time
     * @param nanoTime the current time in nanoseconds
     */
    void render(Camera camera, float tickDelta, long nanoTime);

    /**
     * Called at the end of each client tick.
     */
    void onTick();

    /**
     * Sets the block position for strike effect.
     *
     * @param position the block position, or null to clear
     */
    void setBlockPosition(Vector3f position);

    /**
     * Gets the current block position.
     *
     * @return the current block position, or null if not set
     */
    Vector3f getBlockPosition();

    /**
     * Stops any active animation.
     */
    void stopAnimation();

    /**
     * Gets the provider name for logging purposes.
     *
     * @return the provider name
     */
    String getProviderName();

    /**
     * Disposes of any resources used by this provider.
     * Called during shutdown.
     */
    default void dispose() {
        // Default no-op implementation
    }
}
