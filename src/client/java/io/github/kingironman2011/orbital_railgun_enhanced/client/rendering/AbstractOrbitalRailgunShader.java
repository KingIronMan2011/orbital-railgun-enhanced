package io.github.kingironman2011.orbital_railgun_enhanced.client.rendering;

import io.github.kingironman2011.orbital_railgun_enhanced.client.compat.IrisCompatibilityHelper;
import ladysnake.satin.api.event.PostWorldRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform3f;
import ladysnake.satin.api.managed.uniform.UniformMat4;
import ladysnake.satin.api.util.GlMatrices;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public abstract class AbstractOrbitalRailgunShader
        implements PostWorldRenderCallback, ClientTickEvents.EndTick {
    protected final MinecraftClient client = MinecraftClient.getInstance();

    private final Matrix4f projectionMatrix = new Matrix4f();

    protected final ManagedShaderEffect SHADER =
            ShaderEffectManager.getInstance()
                    .manage(
                            getIdentifier(),
                            shader -> {
                                shader.setSamplerUniform(
                                        "DepthSampler",
                                        ((ReadableDepthFramebuffer) client.getFramebuffer()).getStillDepthMap());
                            });
    private final UniformMat4 uniformInverseTransformMatrix =
            SHADER.findUniformMat4("InverseTransformMatrix");
    private final Uniform3f uniformCameraPosition = SHADER.findUniform3f("CameraPosition");
    private final Uniform1f uniformiTime = SHADER.findUniform1f("iTime");
    protected final Uniform3f uniformBlockPosition = SHADER.findUniform3f("BlockPosition");

    protected int ticks = 0;

    protected abstract Identifier getIdentifier();

    protected abstract boolean shouldRender();

    /**
     * Checks if Satin shaders can be used.
     * When Iris has an active shaderpack, Satin rendering is disabled and the
     * ShaderCompatibilityManager will use the particle fallback instead.
     *
     * @return true if Satin rendering is allowed
     */
    protected boolean canUseSatinShaders() {
        return !IrisCompatibilityHelper.shouldDisableSatinShaders();
    }

    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        // Only count ticks when Satin shaders are usable
        if (shouldRender() && canUseSatinShaders()) {
            ticks++;
        } else if (!canUseSatinShaders()) {
            // Reset ticks when Iris shaderpack is active (fallback handles rendering)
            ticks = 0;
        } else {
            ticks = 0;
        }
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        // Skip Satin rendering when Iris shaderpack is active
        // The ShaderCompatibilityManager fallback provider will handle rendering
        if (!canUseSatinShaders()) {
            return;
        }

        if (shouldRender()) {
            uniformInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(projectionMatrix));
            uniformCameraPosition.set(camera.getPos().toVector3f());
            uniformiTime.set((ticks + tickDelta) / 20f);

            SHADER.render(tickDelta);
        }
    }
}
