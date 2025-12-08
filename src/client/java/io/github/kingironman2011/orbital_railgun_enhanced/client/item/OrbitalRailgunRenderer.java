package io.github.kingironman2011.orbital_railgun_enhanced.client.item;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.client.utils.ModDetector;
import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Custom renderer for the Orbital Railgun item.
 * Switches between custom GeckoLib rendering and standard RenderLayer
 * based on whether Iris shader packs are active.
 */
public class OrbitalRailgunRenderer extends GeoItemRenderer<OrbitalRailgunItem> {
    public OrbitalRailgunRenderer() {
        super(new DefaultedItemGeoModel<>(Identifier.of(OrbitalRailgun.MOD_ID, "orbital_railgun")));
    }

    /**
     * Override the render type to provide shader pack compatibility.
     * When Iris shader packs are active, use a standard cutout layer for better compatibility.
     * Otherwise, use the default GeckoLib rendering.
     * 
     * @param animatable The item being rendered
     * @param texture The texture identifier
     * @return The appropriate RenderLayer based on shader pack status
     */
    @Override
    public RenderLayer getRenderType(OrbitalRailgunItem animatable, Identifier texture) {
        // Check if Iris shader pack is active
        if (ModDetector.isShaderPackActive()) {
            // Use standard cutout layer for shader pack compatibility
            // This ensures proper rendering when custom shaders are in use
            return RenderLayer.getCutout();
        }
        
        // Use default GeckoLib rendering when no shader pack is active
        return super.getRenderType(animatable, texture);
    }
}
