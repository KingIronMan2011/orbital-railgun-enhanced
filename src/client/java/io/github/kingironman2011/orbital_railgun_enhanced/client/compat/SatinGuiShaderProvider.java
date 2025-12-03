package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItem;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform3f;
import ladysnake.satin.api.managed.uniform.UniformMat4;
import ladysnake.satin.api.util.GlMatrices;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Satin-based shader provider for the orbital railgun GUI overlay effect.
 * This provider handles the targeting overlay shader when aiming the railgun.
 */
public class SatinGuiShaderProvider implements ShaderProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    private static final Identifier SHADER_ID =
            Identifier.of(OrbitalRailgun.MOD_ID, "shaders/post/orbital_railgun_enhanced_gui.json");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Matrix4f projectionMatrix = new Matrix4f();

    private ManagedShaderEffect shader;
    private UniformMat4 uniformInverseTransformMatrix;
    private Uniform3f uniformCameraPosition;
    private Uniform1f uniformiTime;
    private Uniform3f uniformBlockPosition;
    private Uniform1f uniformIsBlockHit;

    private Vector3f blockPosition = null;
    private HitResult hitResult = null;
    private int ticks = 0;
    private boolean initialized = false;
    private boolean hudWasHidden = false;

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
            uniformIsBlockHit = shader.findUniform1f("IsBlockHit");

            initialized = true;
            LOGGER.info("Satin GUI shader provider initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Satin GUI shader provider: {}", e.getMessage());
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
        return client.player != null
                && client.player.getActiveItem().getItem() instanceof OrbitalRailgunItem;
    }

    @Override
    public void render(Camera camera, float tickDelta, long nanoTime) {
        if (!shouldRender()) {
            return;
        }

        try {
            hitResult = client.player.raycast(300f, tickDelta, false);

            switch (hitResult.getType()) {
                case BLOCK:
                    uniformIsBlockHit.set(1);
                    uniformBlockPosition.set(
                            ((BlockHitResult) hitResult).getBlockPos().toCenterPos().toVector3f());
                    break;
                case ENTITY:
                    uniformIsBlockHit.set(1);
                    uniformBlockPosition.set(
                            ((EntityHitResult) hitResult).getEntity().getBlockPos().toCenterPos().toVector3f());
                    break;
                case MISS:
                default:
                    uniformIsBlockHit.set(0);
                    break;
            }

            uniformInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(projectionMatrix));
            uniformCameraPosition.set(camera.getPos().toVector3f());
            uniformiTime.set((ticks + tickDelta) / 20f);

            shader.render(tickDelta);
        } catch (Exception e) {
            LOGGER.debug("Error rendering Satin GUI shader: {}", e.getMessage());
        }
    }

    @Override
    public void onTick() {
        if (shouldRender()) {
            client.options.hudHidden = true;
            hudWasHidden = true;
            ticks++;
        } else {
            if (hudWasHidden && ticks != 0) {
                client.options.hudHidden = false;
                hudWasHidden = false;
            }
            ticks = 0;
        }
    }

    @Override
    public void setBlockPosition(Vector3f position) {
        this.blockPosition = position;
    }

    @Override
    public Vector3f getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the current hit result from the raycast.
     *
     * @return the hit result
     */
    public HitResult getHitResult() {
        return hitResult;
    }

    @Override
    public void stopAnimation() {
        blockPosition = null;
        ticks = 0;
        if (hudWasHidden) {
            client.options.hudHidden = false;
            hudWasHidden = false;
        }
    }

    @Override
    public String getProviderName() {
        return "SatinGUI";
    }

    @Override
    public void dispose() {
        if (hudWasHidden) {
            client.options.hudHidden = false;
        }
        initialized = false;
    }
}
