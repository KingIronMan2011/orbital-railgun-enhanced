package io.github.kingironman2011.orbital_railgun_enhanced.client.rendering;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItem;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class OrbitalRailgunGuiShader extends AbstractOrbitalRailgunShader {
    public static final Identifier ORBITAL_RAILGUN_GUI_SHADER =
            Identifier.of(OrbitalRailgun.MOD_ID, "shaders/post/orbital_railgun_enhanced_gui.json");
    public static final OrbitalRailgunGuiShader INSTANCE = new OrbitalRailgunGuiShader();

    private final Uniform1f uniformIsBlockHit = SHADER.findUniform1f("IsBlockHit");

    public HitResult hitResult;

    @Override
    protected Identifier getIdentifier() {
        return ORBITAL_RAILGUN_GUI_SHADER;
    }

    @Override
    protected boolean shouldRender() {
        return client.player != null
                && client.player.getActiveItem().getItem() instanceof OrbitalRailgunItem;
    }

    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        // is it jank to disable the hud rendering here? yeah kinda
        if (shouldRender()) {
            this.client.options.hudHidden = true;
        } else if (ticks != 0) {
            this.client.options.hudHidden = false;
        }

        super.onEndTick(minecraftClient);
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (shouldRender()) {
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
                    uniformIsBlockHit.set(0);
                    break;
                default:
                    uniformIsBlockHit.set(0);
                    break;
            }
        }

        super.onWorldRendered(camera, tickDelta, nanoTime);
    }
    
    /**
     * Fallback rendering using standard vertex format when Iris shader packs are active.
     * Renders a simple crosshair/targeting indicator using cutout layer.
     */
    @Override
    protected void renderFallbackEffect(MatrixStack matrices, VertexConsumer buffer,
                                         Camera camera, float tickDelta) {
        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        
        Vector3f targetPos = uniformBlockPosition.get();
        if (targetPos == null) {
            return;
        }
        
        Vec3d cameraPos = camera.getPos();
        matrices.push();
        
        // Translate to target position relative to camera
        matrices.translate(
            targetPos.x - cameraPos.x,
            targetPos.y - cameraPos.y,
            targetPos.z - cameraPos.z
        );
        
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        
        // Render a simple targeting reticle using vertex format POSITION_COLOR_TEXTURE_LIGHT_NORMAL
        float time = (ticks + tickDelta) / 20f;
        float pulse = (float) (Math.sin(time * 4) * 0.3 + 0.7);
        
        // Color indicates lock-on status (green for locked)
        int color = hitResult.getType() == HitResult.Type.MISS ? 0xFFFF00 : 0x00FF00;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (pulse * 255);
        
        // Render targeting reticle with POSITION_COLOR_TEXTURE_LIGHT_NORMAL format
        int light = 0xF000F0; // Full brightness
        float size = 1.5f;
        float thickness = 0.08f;
        float gap = 0.3f;
        
        // Top line
        buffer.vertex(positionMatrix, -thickness, gap, 0).color(r, g, b, a).texture(0, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, thickness, gap, 0).color(r, g, b, a).texture(1, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, thickness, gap + size, 0).color(r, g, b, a).texture(1, 1).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, -thickness, gap + size, 0).color(r, g, b, a).texture(0, 1).light(light).normal(0, 0, 1).next();
        
        // Bottom line
        buffer.vertex(positionMatrix, -thickness, -gap - size, 0).color(r, g, b, a).texture(0, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, thickness, -gap - size, 0).color(r, g, b, a).texture(1, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, thickness, -gap, 0).color(r, g, b, a).texture(1, 1).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, -thickness, -gap, 0).color(r, g, b, a).texture(0, 1).light(light).normal(0, 0, 1).next();
        
        // Left line
        buffer.vertex(positionMatrix, -gap - size, -thickness, 0).color(r, g, b, a).texture(0, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, -gap, -thickness, 0).color(r, g, b, a).texture(1, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, -gap, thickness, 0).color(r, g, b, a).texture(1, 1).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, -gap - size, thickness, 0).color(r, g, b, a).texture(0, 1).light(light).normal(0, 0, 1).next();
        
        // Right line
        buffer.vertex(positionMatrix, gap, -thickness, 0).color(r, g, b, a).texture(0, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, gap + size, -thickness, 0).color(r, g, b, a).texture(1, 0).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, gap + size, thickness, 0).color(r, g, b, a).texture(1, 1).light(light).normal(0, 0, 1).next();
        buffer.vertex(positionMatrix, gap, thickness, 0).color(r, g, b, a).texture(0, 1).light(light).normal(0, 0, 1).next();
        
        matrices.pop();
    }
}
