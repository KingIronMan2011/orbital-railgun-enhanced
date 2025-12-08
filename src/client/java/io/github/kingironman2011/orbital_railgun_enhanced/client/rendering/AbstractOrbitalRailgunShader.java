package io.github.kingironman2011.orbital_railgun_enhanced.client.rendering;

import io.github.kingironman2011.orbital_railgun_enhanced.client.utils.ModDetector;
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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

/**
 * Base class for Orbital Railgun post-processing shader effects.
 * Switches between Satin post-processing shaders and standard rendering
 * based on Iris shader pack status for compatibility.
 */
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

    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        if (shouldRender()) {
            ticks++;
        } else {
            ticks = 0;
        }
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (!shouldRender()) {
            return;
        }
        
        // Check if Iris shader pack is active
        if (ModDetector.isShaderPackActive()) {
            // Use fallback rendering with standard RenderLayer when Iris is active
            renderFallback(camera, tickDelta, nanoTime);
        } else {
            // Use Satin post-processing shaders when Iris is not active
            uniformInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(projectionMatrix));
            uniformCameraPosition.set(camera.getPos().toVector3f());
            uniformiTime.set((ticks + tickDelta) / 20f);

            SHADER.render(tickDelta);
        }
    }
    
    /**
     * Fallback rendering method using standard Minecraft RenderLayer.
     * Called when Iris shader packs are active to avoid conflicts.
     * Subclasses can override this to provide custom fallback rendering.
     * 
     * @param camera The camera for rendering
     * @param tickDelta The partial tick time
     * @param nanoTime The current time in nanoseconds
     */
    protected void renderFallback(Camera camera, float tickDelta, long nanoTime) {
        // Default implementation: render a simple visual indicator using standard layers
        // Subclasses should override this to provide appropriate fallback rendering
        
        MatrixStack matrices = new MatrixStack();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        
        // Use standard cutout layer for compatibility with Iris
        RenderLayer layer = RenderLayer.getCutout();
        VertexConsumer buffer = immediate.getBuffer(layer);
        
        // Subclasses will implement their specific fallback rendering here
        renderFallbackEffect(matrices, buffer, camera, tickDelta);
        
        immediate.draw();
    }
    
    /**
     * Renders the fallback visual effect using standard vertex rendering.
     * Subclasses must override this to provide their specific visual effect.
     * 
     * @param matrices The matrix stack for transformations
     * @param buffer The vertex consumer for rendering
     * @param camera The camera for rendering
     * @param tickDelta The partial tick time
     */
    protected abstract void renderFallbackEffect(MatrixStack matrices, VertexConsumer buffer, 
                                                   Camera camera, float tickDelta);
}
