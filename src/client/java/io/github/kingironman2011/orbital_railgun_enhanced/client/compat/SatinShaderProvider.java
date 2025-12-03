package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform3f;
import ladysnake.satin.api.managed.uniform.UniformMat4;
import ladysnake.satin.api.util.GlMatrices;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Satin-based shader provider for the orbital strike effect.
 * This provider uses the Satin API for post-processing shaders.
 * It will be disabled when Iris is present with an active shader pack.
 */
public class SatinShaderProvider implements ShaderProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    private static final Identifier SHADER_ID =
            Identifier.of(OrbitalRailgun.MOD_ID, "shaders/post/orbital_railgun_enhanced.json");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Matrix4f projectionMatrix = new Matrix4f();

    private ManagedShaderEffect shader;
    private UniformMat4 uniformInverseTransformMatrix;
    private Uniform3f uniformCameraPosition;
    private Uniform1f uniformiTime;
    private Uniform3f uniformBlockPosition;

    private Vector3f blockPosition = null;
    private RegistryKey<World> dimension = null;
    private int ticks = 0;
    private boolean initialized = false;

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }

        try {
            shader = ShaderEffectManager.getInstance()
                    .manage(
                            SHADER_ID,
                            shaderEffect -> {
                                shaderEffect.setSamplerUniform(
                                        "DepthSampler",
                                        ((ReadableDepthFramebuffer) client.getFramebuffer())
                                                .getStillDepthMap());
                            });

            uniformInverseTransformMatrix = shader.findUniformMat4("InverseTransformMatrix");
            uniformCameraPosition = shader.findUniform3f("CameraPosition");
            uniformiTime = shader.findUniform1f("iTime");
            uniformBlockPosition = shader.findUniform3f("BlockPosition");

            initialized = true;
            LOGGER.info("Satin shader provider initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Satin shader provider: {}", e.getMessage());
            initialized = false;
        }
    }

    @Override
    public boolean isAvailable() {
        return initialized && !IrisCompatibilityHelper.shouldDisableSatinShaders();
    }

    @Override
    public boolean shouldRender() {
        if (!isAvailable()) {
            return false;
        }
        var world = client.world;
        return blockPosition != null && world != null && world.getRegistryKey() == dimension;
    }

    @Override
    public void render(Camera camera, float tickDelta, long nanoTime) {
        if (!shouldRender()) {
            return;
        }

        try {
            uniformBlockPosition.set(blockPosition);
            uniformInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(projectionMatrix));
            uniformCameraPosition.set(camera.getPos().toVector3f());
            uniformiTime.set((ticks + tickDelta) / 20f);

            shader.render(tickDelta);
        } catch (Exception e) {
            LOGGER.debug("Error rendering Satin shader: {}", e.getMessage());
        }
    }

    @Override
    public void onTick() {
        if (ticks >= 1600
                || client.world == null
                || client.world.getRegistryKey() != dimension) {
            blockPosition = null;
            dimension = null;
        }

        if (shouldRender()) {
            ticks++;
        } else {
            ticks = 0;
        }
    }

    @Override
    public void setBlockPosition(Vector3f position) {
        this.blockPosition = position;
        if (position != null && client.world != null) {
            this.dimension = client.world.getRegistryKey();
        }
    }

    @Override
    public Vector3f getBlockPosition() {
        return blockPosition;
    }

    /**
     * Sets the dimension for the strike effect.
     *
     * @param dim the dimension registry key
     */
    public void setDimension(RegistryKey<World> dim) {
        this.dimension = dim;
    }

    /**
     * Gets the current dimension.
     *
     * @return the dimension registry key
     */
    public RegistryKey<World> getDimension() {
        return dimension;
    }

    @Override
    public void stopAnimation() {
        blockPosition = null;
        dimension = null;
        ticks = 0;
    }

    @Override
    public String getProviderName() {
        return "Satin";
    }

    @Override
    public void dispose() {
        // Satin manages shader lifecycle automatically
        initialized = false;
    }
}
