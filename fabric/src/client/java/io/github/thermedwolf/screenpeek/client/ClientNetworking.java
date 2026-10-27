package io.github.thermedwolf.screenpeek.client;

import io.github.thermedwolf.screenpeek.ScreenPeek;
import io.github.thermedwolf.screenpeek.network.ClientboundScreenPeekPayload;
import io.github.thermedwolf.screenpeek.network.ServerboundScreenPeekPayload;
import io.github.thermedwolf.screenpeek.protocol.ScreenPeekCodec;
import io.github.thermedwolf.screenpeek.protocol.ScreenPeekProtocolException;
import io.github.thermedwolf.screenpeek.protocol.packet.ClientboundPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.HelloPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.PlayerActivityPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.WelcomePacket;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side counterpart of the relay: announces the client with a Hello on
 * every join and consumes relay packets. All handlers run on the client
 * thread.
 */
public final class ClientNetworking {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScreenPeek.MOD_ID);

	private ClientNetworking() {
	}

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ClientRelaySession.reset();
			ClientActivity.reset();
			ClientPlayNetworking.send(new ServerboundScreenPeekPayload(ScreenPeekCodec.encode(new HelloPacket())));
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ClientRelaySession.reset();
			ClientActivity.reset();
		});

		ClientPlayNetworking.registerGlobalReceiver(ClientboundScreenPeekPayload.TYPE, (payload, context) -> {
			try {
				ClientboundPacket packet = ScreenPeekCodec.decodeClientbound(payload.encoded());
				switch (packet) {
					case WelcomePacket _ -> {
						ClientRelaySession.markPresent();
						LOGGER.debug("ScreenPeek relay present on this server");
					}
					case PlayerActivityPacket p -> {
					ClientActivity.PEERS.update(p.playerId(), p.state());
					LOGGER.debug("Peer {} -> typing={} menu={} idle={}",
							p.playerId(), p.state().typing(), p.state().openMenu(), p.state().idle());
				}
				}
			} catch (ScreenPeekProtocolException e) {
				LOGGER.debug("Dropped undecodable ScreenPeek packet: {}", e.getMessage());
			}
		});
	}
}
