package io.github.kingironman2011.orbital_railgun_enhanced.network;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ClientSyncPayload(BlockPos blockPos) implements CustomPayload {

    public static final Id<ClientSyncPayload> ID =
            new Id<>(Identifier.of(OrbitalRailgun.MOD_ID, "client_sync_packet"));

    public static final PacketCodec<RegistryByteBuf, ClientSyncPayload> CODEC =
            PacketCodec.tuple(BlockPos.PACKET_CODEC, ClientSyncPayload::blockPos, ClientSyncPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
