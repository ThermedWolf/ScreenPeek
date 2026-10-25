package io.github.thermedwolf.screenpeek.network;

import io.github.thermedwolf.screenpeek.ScreenPeek;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Transport envelope for everything the relay sends to ScreenPeek clients.
 * The body is opaque {@code byte[]} — encoding lives in the protocol module.
 */
public record ClientboundScreenPeekPayload(byte[] encoded) implements CustomPacketPayload {

	public static final Type<ClientboundScreenPeekPayload> TYPE = new Type<>(ScreenPeek.id("clientbound"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundScreenPeekPayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.byteArray(ScreenPeekNetworking.MAX_PAYLOAD_SIZE_BYTES),
					ClientboundScreenPeekPayload::encoded,
					ClientboundScreenPeekPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
