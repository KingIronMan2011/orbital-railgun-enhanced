package io.github.kingironman2011.orbital_railgun_enhanced.client.compat;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for detecting Iris shaders mod presence and state.
 * This allows the mod to gracefully handle shader compatibility when Iris is installed.
 */
public final class IrisCompatibilityHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    private static final String IRIS_MOD_ID = "iris";

    private static Boolean irisPresent = null;
    private static Boolean shaderPackActive = null;

    private IrisCompatibilityHelper() {
        // Utility class
    }

    /**
     * Checks if Iris is installed.
     *
     * @return true if Iris is present
     */
    public static boolean isIrisPresent() {
        if (irisPresent == null) {
            irisPresent = FabricLoader.getInstance().isModLoaded(IRIS_MOD_ID);
            if (irisPresent) {
                LOGGER.info("Iris detected - shader compatibility mode enabled");
            }
        }
        return irisPresent;
    }

    /**
     * Attempts to detect if an Iris shader pack is currently active.
     * Uses reflection to avoid hard dependency on Iris.
     *
     * @return true if a shader pack is active, false if not or if detection fails
     */
    public static boolean isShaderPackActive() {
        if (!isIrisPresent()) {
            return false;
        }

        // Always check fresh - shader pack status can change at runtime
        boolean previousStatus = shaderPackActive != null ? shaderPackActive : false;

        try {
            // Try to access Iris's IrisApi to check shader pack status
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = irisApiClass.getMethod("getInstance").invoke(null);
            Boolean isActive = (Boolean) irisApiClass.getMethod("isShaderPackInUse").invoke(instance);
            shaderPackActive = isActive != null && isActive;
        } catch (ClassNotFoundException e) {
            // Iris API not available - try alternative detection
            shaderPackActive = tryAlternativeDetection();
        } catch (NoSuchMethodException e) {
            // API method signature changed - try alternative detection
            LOGGER.debug("Iris API method signature changed, trying alternative: {}", e.getMessage());
            shaderPackActive = tryAlternativeDetection();
        } catch (ReflectiveOperationException e) {
            // General reflection failure - assume shaders are not active for safety
            LOGGER.debug("Failed to detect Iris shader pack status: {}", e.getMessage());
            shaderPackActive = false;
        } catch (Exception e) {
            // Catch-all for any unexpected issues to prevent crashes
            LOGGER.warn("Unexpected error detecting Iris shader pack status: {}", e.getMessage());
            shaderPackActive = false;
        }

        // Log when status changes
        if (previousStatus != shaderPackActive) {
            LOGGER.info("Iris shader pack status changed: {} -> {}", previousStatus, shaderPackActive);
        }

        return shaderPackActive;
    }

    /**
     * Alternative method to detect shader pack status when Iris API is not available.
     *
     * @return true if shader pack appears to be active
     */
    private static boolean tryAlternativeDetection() {
        try {
            // Try older Iris API location
            Class<?> irisClass = Class.forName("net.coderbot.iris.Iris");
            Object currentPackId = irisClass.getMethod("getCurrentPackName").invoke(null);
            return currentPackId != null && !currentPackId.toString().isEmpty();
        } catch (ReflectiveOperationException e) {
            LOGGER.debug("Alternative Iris detection failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if Satin shaders should be disabled due to Iris compatibility issues.
     * This returns true when both Iris is present AND a shader pack is active.
     *
     * @return true if Satin shaders should be disabled
     */
    public static boolean shouldDisableSatinShaders() {
        return isIrisPresent() && isShaderPackActive();
    }

    /**
     * Resets the cached detection values.
     * Useful for when shader pack status might have changed.
     */
    public static void resetCache() {
        shaderPackActive = null;
    }

    /**
     * Gets a human-readable status string for debugging.
     *
     * @return status string describing Iris compatibility state
     */
    public static String getStatusString() {
        if (!isIrisPresent()) {
            return "Iris not detected - using Satin shaders";
        }
        if (isShaderPackActive()) {
            return "Iris shader pack active - Satin shaders disabled for compatibility";
        }
        return "Iris detected but no shader pack active - using Satin shaders";
    }
}
