package io.github.kingironman2011.orbital_railgun_enhanced.network;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StopAreaSoundPayload(Identifier soundId) implements CustomPayload {

    public static final Id<StopAreaSoundPayload> ID =
            new Id<>(Identifier.of(OrbitalRailgun.MOD_ID, "stop_area_sound"));

    public static final PacketCodec<RegistryByteBuf, StopAreaSoundPayload> CODEC =
            PacketCodec.tuple(
                    Identifier.PACKET_CODEC, StopAreaSoundPayload::soundId, StopAreaSoundPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
