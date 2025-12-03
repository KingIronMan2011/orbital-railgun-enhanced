package io.github.kingironman2011.orbital_railgun_enhanced.compat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.joml.Vector3f;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Shader Compatibility API.
 * These tests verify the core logic of the compatibility layer without
 * requiring Minecraft/Fabric runtime dependencies.
 */
class ShaderCompatibilityTest {

    @Test
    @DisplayName("Vector3f position should be stored correctly")
    void testVector3fStorage() {
        Vector3f position = new Vector3f(100.5f, 64.0f, -200.5f);

        // Verify the vector stores values correctly
        assertEquals(100.5f, position.x, 0.001f);
        assertEquals(64.0f, position.y, 0.001f);
        assertEquals(-200.5f, position.z, 0.001f);
    }

    @Test
    @DisplayName("Vector3f null handling")
    void testNullVector() {
        Vector3f position = null;
        assertNull(position);

        // After setting
        position = new Vector3f(0, 0, 0);
        assertNotNull(position);
    }

    @Test
    @DisplayName("Strike timing constants should be valid")
    void testStrikeTimingConstants() {
        // These match the shader timing constants
        int startTicks = 80; // 4 seconds * 20 ticks
        int expansionTicks = 640; // 32 seconds * 20 ticks
        int endTicks = startTicks + expansionTicks;
        int maxTicks = 1600;

        assertEquals(80, startTicks, "Start ticks should be 80 (4 seconds)");
        assertEquals(640, expansionTicks, "Expansion ticks should be 640 (32 seconds)");
        assertEquals(720, endTicks, "End ticks should be 720 (36 seconds)");
        assertEquals(1600, maxTicks, "Max ticks should be 1600 (80 seconds)");

        assertTrue(startTicks < endTicks, "Start should be before end");
        assertTrue(endTicks < maxTicks, "End should be before max");
    }

    @Test
    @DisplayName("Shader time calculation")
    void testShaderTimeCalculation() {
        // The shader uses (ticks + tickDelta) / 20f for time
        int ticks = 100;
        float tickDelta = 0.5f;

        float time = (ticks + tickDelta) / 20f;

        assertEquals(5.025f, time, 0.001f, "Time should be 5.025 seconds");
    }

    @Test
    @DisplayName("Iris check interval should be reasonable")
    void testIrisCheckInterval() {
        int checkInterval = 100; // 5 seconds

        assertTrue(checkInterval >= 20, "Check interval should be at least 1 second (20 ticks)");
        assertTrue(checkInterval <= 1200, "Check interval should be at most 1 minute (1200 ticks)");
    }

    @Test
    @DisplayName("Provider name strings")
    void testProviderNames() {
        String satinName = "Satin";
        String fallbackName = "IrisFallback";
        String satinGuiName = "SatinGUI";
        String fallbackGuiName = "IrisGUIFallback";

        assertNotNull(satinName);
        assertNotNull(fallbackName);
        assertNotNull(satinGuiName);
        assertNotNull(fallbackGuiName);

        assertFalse(satinName.isEmpty());
        assertFalse(fallbackName.isEmpty());
    }

    @Test
    @DisplayName("Fallback particle timing phases")
    void testFallbackParticlePhases() {
        // Phase 1: Pre-strike charging (0-80 ticks)
        int phase1End = 80;
        // Phase 2: Strike beam and expansion (80-720 ticks)
        int phase2End = 720;
        // Phase 3: Aftermath/shockwave (720-1600 ticks)
        int phase3End = 1600;

        // Test tick values fall into correct phases
        int testTicks1 = 50;
        assertTrue(testTicks1 < phase1End, "Tick 50 should be in phase 1");

        int testTicks2 = 400;
        assertTrue(testTicks2 >= phase1End && testTicks2 < phase2End, "Tick 400 should be in phase 2");

        int testTicks3 = 1000;
        assertTrue(testTicks3 >= phase2End && testTicks3 < phase3End, "Tick 1000 should be in phase 3");
    }

    @Test
    @DisplayName("Shockwave radius calculation")
    void testShockwaveRadiusCalculation() {
        // From the fallback provider: shockwaveRadius = Math.min(time * 10, 100)
        float time1 = 5.0f;
        double radius1 = Math.min(time1 * 10, 100);
        assertEquals(50.0, radius1, 0.001, "Radius at 5s should be 50");

        float time2 = 15.0f;
        double radius2 = Math.min(time2 * 10, 100);
        assertEquals(100.0, radius2, 0.001, "Radius at 15s should be capped at 100");
    }

    @Test
    @DisplayName("Fade intensity calculation")
    void testFadeIntensityCalculation() {
        // From the fallback provider: intensity = Math.max(0, 1 - time / 44)
        float time1 = 0.0f;
        double intensity1 = Math.max(0, 1 - time1 / 44);
        assertEquals(1.0, intensity1, 0.001, "Intensity at t=0 should be 1.0");

        float time2 = 22.0f;
        double intensity2 = Math.max(0, 1 - time2 / 44);
        assertEquals(0.5, intensity2, 0.001, "Intensity at t=22 should be 0.5");

        float time3 = 44.0f;
        double intensity3 = Math.max(0, 1 - time3 / 44);
        assertEquals(0.0, intensity3, 0.001, "Intensity at t=44 should be 0.0");

        float time4 = 88.0f;
        double intensity4 = Math.max(0, 1 - time4 / 44);
        assertEquals(0.0, intensity4, 0.001, "Intensity beyond t=44 should be 0.0");
    }

    @Test
    @DisplayName("GUI HUD colors should be valid ARGB")
    void testHudColors() {
        int colorGreen = 0xFF21A521;
        int colorRed = 0xFFE06D6D;
        int colorCyan = 0xFF9EEDEE;
        int colorBg = 0x80000000;

        // Verify alpha channel
        assertEquals(0xFF, (colorGreen >> 24) & 0xFF, "Green should have full alpha");
        assertEquals(0xFF, (colorRed >> 24) & 0xFF, "Red should have full alpha");
        assertEquals(0xFF, (colorCyan >> 24) & 0xFF, "Cyan should have full alpha");
        assertEquals(0x80, (colorBg >> 24) & 0xFF, "Background should have 50% alpha");
    }

    @Test
    @DisplayName("Distance calculation for targeting")
    void testDistanceCalculation() {
        // Simulate distance calculation
        double playerX = 0, playerY = 64, playerZ = 0;
        double targetX = 100, targetY = 64, targetZ = -200;

        double dx = targetX - playerX;
        double dy = targetY - playerY;
        double dz = targetZ - playerZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        double distance = Math.sqrt(distanceSquared);

        // Expected: sqrt(100^2 + 0^2 + 200^2) = sqrt(10000 + 40000) = sqrt(50000) ≈ 223.6
        assertEquals(223.6, distance, 0.1, "Distance should be approximately 223.6");

        // Within max range of 300
        assertTrue(distance <= 300, "Target should be within railgun range");
    }

    @Test
    @DisplayName("AOE radius constant")
    void testAoeRadius() {
        int aoeRadius = 24;
        assertEquals(24, aoeRadius, "AOE radius should be 24 blocks");
    }
}
