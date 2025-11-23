package io.github.kingironman2011.orbital_railgun.client.item;

import io.github.kingironman2011.orbital_railgun.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun.item.OrbitalRailgunItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class OrbitalRailgunRenderer extends GeoItemRenderer<OrbitalRailgunItem> {
    public OrbitalRailgunRenderer() {
        super(new DefaultedItemGeoModel<>(Identifier.of(OrbitalRailgun.MOD_ID, "orbital_railgun")));
    }
}
