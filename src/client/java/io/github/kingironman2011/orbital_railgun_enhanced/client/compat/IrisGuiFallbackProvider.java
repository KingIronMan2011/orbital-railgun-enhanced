package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fallback GUI provider that uses simple HUD rendering when Iris is active.
 * This provides a basic targeting UI without using Satin shaders.
 */
public class IrisGuiFallbackProvider implements ShaderProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");

    private final MinecraftClient client = MinecraftClient.getInstance();

    private Vector3f blockPosition = null;
    private HitResult hitResult = null;
    private int ticks = 0;
    private boolean initialized = false;

    // Colors for the HUD (ARGB format)
    private static final int COLOR_GREEN = 0xFF21A521;
    private static final int COLOR_RED = 0xFFE06D6D;
    private static final int COLOR_CYAN = 0xFF9EEDEE;
    private static final int COLOR_BG = 0x80000000;

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        LOGGER.info("Iris GUI fallback provider initialized");
    }

    @Override
    public boolean isAvailable() {
        return initialized && IrisCompatibilityHelper.shouldDisableSatinShaders();
    }

    @Override
    public boolean shouldRender() {
        return client.player != null
                && client.player.getActiveItem().getItem() instanceof OrbitalRailgunItem;
    }

    @Override
    public void render(Camera camera, float tickDelta, long nanoTime) {
        // This method is for post-world rendering, but the fallback uses HUD rendering
        // The actual rendering happens in renderHud() which should be called separately
        if (!shouldRender() || client.player == null) {
            return;
        }

        hitResult = client.player.raycast(300f, tickDelta, false);
    }

    /**
     * Renders the fallback targeting HUD overlay.
     * This should be called from a HUD render event.
     *
     * @param context the draw context
     * @param tickDelta the partial tick time
     */
    public void renderHud(DrawContext context, float tickDelta) {
        if (!shouldRender() || !isAvailable()) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Calculate animation progress
        float progress = Math.min(1.0f, ticks / 20.0f);

        // Draw outer frame with animation
        int frameWidth = (int) (screenWidth * 0.9f * progress);
        int frameHeight = (int) (screenHeight * 0.9f * progress);
        int frameX = centerX - frameWidth / 2;
        int frameY = centerY - frameHeight / 2;

        // Draw corner brackets
        int bracketLength = 20;
        int bracketThickness = 2;

        // Top-left corner
        context.fill(frameX, frameY, frameX + bracketLength, frameY + bracketThickness, COLOR_GREEN);
        context.fill(frameX, frameY, frameX + bracketThickness, frameY + bracketLength, COLOR_GREEN);

        // Top-right corner
        context.fill(frameX + frameWidth - bracketLength, frameY,
                frameX + frameWidth, frameY + bracketThickness, COLOR_GREEN);
        context.fill(frameX + frameWidth - bracketThickness, frameY,
                frameX + frameWidth, frameY + bracketLength, COLOR_GREEN);

        // Bottom-left corner
        context.fill(frameX, frameY + frameHeight - bracketThickness,
                frameX + bracketLength, frameY + frameHeight, COLOR_GREEN);
        context.fill(frameX, frameY + frameHeight - bracketLength,
                frameX + bracketThickness, frameY + frameHeight, COLOR_GREEN);

        // Bottom-right corner
        context.fill(frameX + frameWidth - bracketLength, frameY + frameHeight - bracketThickness,
                frameX + frameWidth, frameY + frameHeight, COLOR_GREEN);
        context.fill(frameX + frameWidth - bracketThickness, frameY + frameHeight - bracketLength,
                frameX + frameWidth, frameY + frameHeight, COLOR_GREEN);

        // Draw crosshair
        int crosshairSize = 15;
        context.fill(centerX - crosshairSize, centerY - 1,
                centerX - 5, centerY + 1, COLOR_GREEN);
        context.fill(centerX + 5, centerY - 1,
                centerX + crosshairSize, centerY + 1, COLOR_GREEN);
        context.fill(centerX - 1, centerY - crosshairSize,
                centerX + 1, centerY - 5, COLOR_GREEN);
        context.fill(centerX - 1, centerY + 5,
                centerX + 1, centerY + crosshairSize, COLOR_GREEN);

        // Draw target indicator if we have a hit
        if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            BlockPos targetPos = getTargetBlockPos();
            if (targetPos != null) {
                // Draw target info
                TextRenderer textRenderer = client.textRenderer;

                String targetText = String.format("TARGET: %d, %d, %d",
                        targetPos.getX(), targetPos.getY(), targetPos.getZ());

                // Calculate distance
                double distance = Math.sqrt(
                        client.player.getBlockPos().getSquaredDistance(targetPos));
                String distanceText = String.format("DISTANCE: %.1fm", distance);

                // AOE radius indicator
                String aoeText = "AOE RADIUS: 24 BLOCKS";

                int textY = frameY + 10;
                int textColor = distance <= 300 ? COLOR_GREEN : COLOR_RED;

                // Draw text with background
                drawTextWithBackground(context, textRenderer, targetText, frameX + 10, textY, textColor);
                drawTextWithBackground(context, textRenderer, distanceText, frameX + 10, textY + 12, textColor);
                drawTextWithBackground(context, textRenderer, aoeText, frameX + 10, textY + 24, COLOR_CYAN);

                // Draw status
                String statusText = hitResult.getType() == HitResult.Type.ENTITY
                        ? "ENTITY LOCKED" : "TERRAIN LOCKED";
                int statusColor = hitResult.getType() == HitResult.Type.ENTITY
                        ? COLOR_RED : COLOR_GREEN;
                drawTextWithBackground(context, textRenderer, statusText, frameX + 10, textY + 40, statusColor);

                // Draw "IRIS COMPAT MODE" indicator
                drawTextWithBackground(context, textRenderer, "[IRIS COMPAT MODE]",
                        frameX + frameWidth - 100, textY, 0xFFFFAA00);
            }
        } else {
            // No target
            TextRenderer textRenderer = client.textRenderer;
            drawTextWithBackground(context, textRenderer, "NO TARGET",
                    centerX - 25, centerY + 30, COLOR_RED);
        }

        // Scanline effect simulation (subtle)
        int scanlineY = (int) ((ticks * 2 + tickDelta) % screenHeight);
        context.fill(0, scanlineY, screenWidth, scanlineY + 1, 0x10FFFFFF);
    }

    private void drawTextWithBackground(DrawContext context, TextRenderer textRenderer,
                                         String text, int x, int y, int color) {
        int width = textRenderer.getWidth(text);
        context.fill(x - 2, y - 1, x + width + 2, y + 10, COLOR_BG);
        context.drawText(textRenderer, Text.literal(text), x, y, color, false);
    }

    private BlockPos getTargetBlockPos() {
        if (hitResult == null) {
            return null;
        }

        return switch (hitResult.getType()) {
            case BLOCK -> ((BlockHitResult) hitResult).getBlockPos();
            case ENTITY -> ((EntityHitResult) hitResult).getEntity().getBlockPos();
            default -> null;
        };
    }

    @Override
    public void onTick() {
        if (shouldRender()) {
            ticks++;
        } else {
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
    }

    @Override
    public String getProviderName() {
        return "IrisGUIFallback";
    }

    @Override
    public void dispose() {
        initialized = false;
    }
}
