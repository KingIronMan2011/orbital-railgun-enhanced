package io.github.kingironman2011.orbital_railgun_enhanced.client;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.client.item.OrbitalRailgunRenderer;
import io.github.kingironman2011.orbital_railgun_enhanced.client.rendering.OrbitalRailgunGuiShader;
import io.github.kingironman2011.orbital_railgun_enhanced.client.rendering.OrbitalRailgunShader;
import io.github.kingironman2011.orbital_railgun_enhanced.item.OrbitalRailgunItems;
import ladysnake.satin.api.event.PostWorldRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.client.RenderProvider;

public class OrbitalRailgunClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OrbitalRailgunItems.ORBITAL_RAILGUN.renderProviderHolder.setValue(new RenderProvider() {
            private OrbitalRailgunRenderer renderer;

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new OrbitalRailgunRenderer();
                }

                return this.renderer;
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(OrbitalRailgun.CLIENT_SYNC_PACKET_ID, ((minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> {
            BlockPos blockPos = packetByteBuf.readBlockPos();

            minecraftClient.execute(() -> {
                OrbitalRailgunShader.INSTANCE.BlockPosition = blockPos.toCenterPos().toVector3f();
                OrbitalRailgunShader.INSTANCE.Dimension = minecraftClient.world.getRegistryKey();
            });
        }));

        ClientTickEvents.END_CLIENT_TICK.register(OrbitalRailgunGuiShader.INSTANCE);
        PostWorldRenderCallback.EVENT.register(OrbitalRailgunGuiShader.INSTANCE);

        ClientTickEvents.END_CLIENT_TICK.register(OrbitalRailgunShader.INSTANCE);
        PostWorldRenderCallback.EVENT.register(OrbitalRailgunShader.INSTANCE);
    }
}
