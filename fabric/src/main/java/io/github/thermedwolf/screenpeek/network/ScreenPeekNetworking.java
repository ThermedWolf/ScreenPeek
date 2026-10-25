package io.github.thermedwolf.screenpeek.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Registers ScreenPeek's transport payloads on both play directions. Receivers
 * and senders attach to these types in later steps; this call must happen on
 * both logical sides, which the common initializer covers.
 */
public final class ScreenPeekNetworking {

	/**
	 * Hard cap on a single ScreenPeek packet body. Our messages are a few dozen
	 * bytes; anything larger is corruption or abuse and is rejected at the
	 * transport instead of being decoded.
	 */
	public static final int MAX_PAYLOAD_SIZE_BYTES = 256;

	private ScreenPeekNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(ServerboundScreenPeekPayload.TYPE, ServerboundScreenPeekPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ClientboundScreenPeekPayload.TYPE, ClientboundScreenPeekPayload.CODEC);
	}
}
