package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fallback shader provider that uses vanilla particle effects when Iris is active.
 * This provides a compatible visual effect without using Satin's post-processing
 * shaders that can conflict with Iris.
 */
public class IrisFallbackProvider implements ShaderProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");

    private final MinecraftClient client = MinecraftClient.getInstance();

    private Vector3f blockPosition = null;
    private RegistryKey<World> dimension = null;
    private int ticks = 0;
    private boolean initialized = false;

    // Effect timing constants (matching Satin shader timing)
    private static final int START_TICKS = 80; // 4 seconds * 20 ticks
    private static final int EXPANSION_TICKS = 640; // 32 seconds * 20 ticks
    private static final int END_TICKS = START_TICKS + EXPANSION_TICKS;
    private static final int MAX_TICKS = 1600;

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        LOGGER.info("Iris fallback provider initialized - using particle effects");
    }

    @Override
    public boolean isAvailable() {
        // This provider is always available as fallback when Iris is active
        return initialized && IrisCompatibilityHelper.shouldDisableSatinShaders();
    }

    @Override
    public boolean shouldRender() {
        var world = client.world;
        return blockPosition != null && world != null && world.getRegistryKey() == dimension;
    }

    @Override
    public void render(Camera camera, float tickDelta, long nanoTime) {
        if (!shouldRender() || client.world == null) {
            return;
        }

        // Create particle-based visual effect as fallback
        // This runs on the render thread, so we use scheduled particles
        spawnStrikeParticles(tickDelta);
    }

    private void spawnStrikeParticles(float tickDelta) {
        if (client.world == null || blockPosition == null) {
            return;
        }

        Vec3d pos = new Vec3d(blockPosition.x, blockPosition.y, blockPosition.z);
        float time = (ticks + tickDelta) / 20f;

        // Phase 1: Pre-strike charging effect (first 4 seconds)
        if (ticks < START_TICKS) {
            spawnChargingEffect(pos, time);
        } else if (ticks < END_TICKS) {
            // Phase 2: Strike beam and expansion (4-36 seconds)
            spawnStrikeEffect(pos, time - 4f);
        } else {
            // Phase 3: Aftermath/shockwave (36-80 seconds)
            spawnAftermathEffect(pos, time - 36f);
        }
    }

    private void spawnChargingEffect(Vec3d pos, float time) {
        if (client.world == null) {
            return;
        }

        // Create a swirling ring of particles
        double radius = 24.0;
        double rotation = time * 4.0;
        int particleCount = 8;

        for (int i = 0; i < particleCount; i++) {
            double angle = rotation + (2 * Math.PI * i / particleCount);
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;

            client.world.addParticle(
                    ParticleTypes.END_ROD,
                    x, pos.y + 2, z,
                    0, 0.1, 0
            );
        }

        // Center beam charging up
        if (time > 3f) {
            for (int y = 0; y < 10; y++) {
                client.world.addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        pos.x, pos.y + y * 5, pos.z,
                        0, 0.5, 0
                );
            }
        }
    }

    private void spawnStrikeEffect(Vec3d pos, float time) {
        if (client.world == null) {
            return;
        }

        // Main strike beam
        for (int y = 0; y < 50; y++) {
            double beamY = pos.y + y * 4;
            client.world.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x + (Math.random() - 0.5) * 2,
                    beamY,
                    pos.z + (Math.random() - 0.5) * 2,
                    0, 1.0, 0
            );
        }

        // Expanding shockwave ring
        double shockwaveRadius = Math.min(time * 10, 100);
        int ringParticles = (int) (shockwaveRadius / 2);

        for (int i = 0; i < ringParticles; i++) {
            double angle = 2 * Math.PI * i / ringParticles;
            double x = pos.x + Math.cos(angle) * shockwaveRadius;
            double z = pos.z + Math.sin(angle) * shockwaveRadius;

            client.world.addParticle(
                    ParticleTypes.END_ROD,
                    x, pos.y + 1, z,
                    Math.cos(angle) * 0.1, 0, Math.sin(angle) * 0.1
            );
        }

        // Impact explosion at center
        if (time < 5f) {
            for (int i = 0; i < 5; i++) {
                client.world.addParticle(
                        ParticleTypes.EXPLOSION,
                        pos.x + (Math.random() - 0.5) * 10,
                        pos.y + Math.random() * 5,
                        pos.z + (Math.random() - 0.5) * 10,
                        0, 0, 0
                );
            }
        }
    }

    private void spawnAftermathEffect(Vec3d pos, float time) {
        if (client.world == null) {
            return;
        }

        // Fading shockwave
        double fadeRadius = 50 + time * 25;
        double intensity = Math.max(0, 1 - time / 44); // Fade over remaining time

        if (intensity <= 0) {
            return;
        }

        int ringParticles = (int) (fadeRadius / 4 * intensity);

        for (int i = 0; i < ringParticles; i++) {
            double angle = 2 * Math.PI * i / ringParticles;
            double x = pos.x + Math.cos(angle) * fadeRadius;
            double z = pos.z + Math.sin(angle) * fadeRadius;

            client.world.addParticle(
                    ParticleTypes.REVERSE_PORTAL,
                    x, pos.y + 1, z,
                    0, 0.05, 0
            );
        }
    }

    @Override
    public void onTick() {
        if (ticks >= MAX_TICKS
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

    @Override
    public void stopAnimation() {
        blockPosition = null;
        dimension = null;
        ticks = 0;
    }

    @Override
    public String getProviderName() {
        return "IrisFallback";
    }

    @Override
    public void dispose() {
        initialized = false;
    }
}
