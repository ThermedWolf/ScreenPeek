package io.github.thermedwolf.screenpeek.network;

import io.github.thermedwolf.screenpeek.ScreenPeek;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Transport envelope for everything ScreenPeek clients send to the relay.
 * The body is opaque {@code byte[]} — encoding lives in the protocol module,
 * so the Paper plugin reads the exact same format without any shared code.
 */
public record ServerboundScreenPeekPayload(byte[] encoded) implements CustomPacketPayload {

	public static final Type<ServerboundScreenPeekPayload> TYPE = new Type<>(ScreenPeek.id("serverbound"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundScreenPeekPayload> CODEC =
			StreamCodec.composite(
					ByteBufCodecs.byteArray(ScreenPeekNetworking.MAX_PAYLOAD_SIZE_BYTES),
					ServerboundScreenPeekPayload::encoded,
					ServerboundScreenPeekPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
