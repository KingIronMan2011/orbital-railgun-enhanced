package io.github.kingironman2011.orbital_railgun_enhanced.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class OrbitalRailgunItems {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrbitalRailgunEnhanced");
    public static final OrbitalRailgunItem ORBITAL_RAILGUN = (OrbitalRailgunItem) register(new OrbitalRailgunItem(), "orbital_railgun");

    public static Item register(Item item, String id) {
		Identifier itemID = new Identifier(OrbitalRailgun.MOD_ID, id);
		Item registeredItem = Registry.register(Registries.ITEM, itemID, item);
		if (ServerConfig.INSTANCE.isDebugMode()) {
		    LOGGER.debug("[REGISTRY] Registered item: {}", itemID);
		}
		return registeredItem;
	}
	
	public static void initialize() {
	    LOGGER.info("Registering items...");
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
		    fabricItemGroupEntries.addAfter(Items.CROSSBOW, ORBITAL_RAILGUN);
		    if (ServerConfig.INSTANCE.isDebugMode()) {
		        LOGGER.debug("[REGISTRY] Added Orbital Railgun to COMBAT item group");
		    }
		});
	}
}
