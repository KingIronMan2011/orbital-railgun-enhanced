package io.github.kingironman2011.orbital_railgun_enhanced.client.rendering;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class OrbitalRailgunShader extends AbstractOrbitalRailgunShader {
    public static final Identifier ORBITAL_RAILGUN_SHADER =
            Identifier.of(OrbitalRailgun.MOD_ID, "shaders/post/orbital_railgun_enhanced.json");
    public static final OrbitalRailgunShader INSTANCE = new OrbitalRailgunShader();

    public Vector3f BlockPosition = null;
    public RegistryKey<World> Dimension = null;

    @Override
    protected Identifier getIdentifier() {
        return ORBITAL_RAILGUN_SHADER;
    }

    @Override
    protected boolean shouldRender() {
        var world = MinecraftClient.getInstance().world;
        return BlockPosition != null && world != null && world.getRegistryKey() == Dimension;
    }

    /**
     * Stops the animation by clearing the block position and dimension.
     * Called when the player leaves the configured range.
     */
    public void stopAnimation() {
        BlockPosition = null;
        Dimension = null;
        ticks = 0;
    }

    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        if (ticks >= 1600
                || minecraftClient.world == null
                || minecraftClient.world.getRegistryKey() != Dimension) {
            BlockPosition = null;
            Dimension = null;
        }

        super.onEndTick(minecraftClient);
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (shouldRender()) {
            uniformBlockPosition.set(BlockPosition);
        }

        super.onWorldRendered(camera, tickDelta, nanoTime);
    }
    
    /**
     * Fallback rendering using standard vertex format when Iris shader packs are active.
     * Renders a simple visual indicator at the strike position using cutout layer.
     */
    @Override
    protected void renderFallbackEffect(MatrixStack matrices, VertexConsumer buffer,
                                         Camera camera, float tickDelta) {
        if (BlockPosition == null) {
            return;
        }
        
        Vec3d cameraPos = camera.getPos();
        matrices.push();
        
        // Translate to strike position relative to camera
        matrices.translate(
            BlockPosition.x - cameraPos.x,
            BlockPosition.y - cameraPos.y,
            BlockPosition.z - cameraPos.z
        );
        
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        
        // Render a simple pulsing effect using vertex format POSITION_COLOR_TEXTURE_LIGHT_NORMAL
        float time = (ticks + tickDelta) / 20f;
        float pulse = (float) (Math.sin(time * 2) * 0.5 + 0.5);
        float size = 2.0f + pulse * 0.5f;
        
        // Color indicates strike effect (red/orange for orbital strike)
        int color = 0xFF6600; // Orange
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (pulse * 255);
        
        // Render a simple quad billboard facing the camera
        // Using POSITION_COLOR_TEXTURE_LIGHT_NORMAL format
        int light = 0xF000F0; // Full brightness
        
        // Simple cross pattern (vertical and horizontal lines)
        float halfSize = size / 2;
        
        // Vertical line (4 vertices for a thin quad)
        buffer.vertex(positionMatrix, -0.1f, -halfSize, 0).color(r, g, b, a).texture(0, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, 0.1f, -halfSize, 0).color(r, g, b, a).texture(1, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, 0.1f, halfSize, 0).color(r, g, b, a).texture(1, 1).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, -0.1f, halfSize, 0).color(r, g, b, a).texture(0, 1).light(light).normal(0, 0, 1).next();
        
        // Horizontal line (4 vertices for a thin quad)
        buffer.vertex(positionMatrix, -halfSize, -0.1f, 0).color(r, g, b, a).texture(0, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, halfSize, -0.1f, 0).color(r, g, b, a).texture(1, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, halfSize, 0.1f, 0).color(r, g, b, a).texture(1, 1).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, -halfSize, 0.1f, 0).color(r, g, b, a).texture(0, 1).light(light).normal(0, 0, 1).next();
        
        matrices.pop();
    }
}
