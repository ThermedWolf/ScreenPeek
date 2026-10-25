package io.github.thermedwolf.screenpeek.relay;

import io.github.thermedwolf.screenpeek.ScreenPeek;
import io.github.thermedwolf.screenpeek.network.ClientboundScreenPeekPayload;
import io.github.thermedwolf.screenpeek.network.ServerboundScreenPeekPayload;
import io.github.thermedwolf.screenpeek.protocol.ScreenPeekCodec;
import io.github.thermedwolf.screenpeek.protocol.ScreenPeekProtocol;
import io.github.thermedwolf.screenpeek.protocol.ScreenPeekProtocolException;
import io.github.thermedwolf.screenpeek.protocol.packet.ActivityUpdatePacket;
import io.github.thermedwolf.screenpeek.protocol.packet.ClientboundPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.HelloPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.ServerboundPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.WelcomePacket;
import io.github.thermedwolf.screenpeek.protocol.relay.RelayTransport;
import io.github.thermedwolf.screenpeek.protocol.relay.ScreenPeekRelay;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Binds the shared {@link ScreenPeekRelay} logic to Fabric's server
 * networking. Every entry point here (packet receivers, connection events,
 * lifecycle events) fires on the server thread, which satisfies the relay's
 * threading contract.
 */
public final class RelayNetworking {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScreenPeek.MOD_ID);

	private static volatile MinecraftServer server;

	private static final FabricRelayTransport TRANSPORT = new FabricRelayTransport();
	private static final ScreenPeekRelay RELAY = new ScreenPeekRelay(TRANSPORT);

	private RelayNetworking() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(started -> server = started);
		ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> {
			server = null;
			RELAY.reset();
		});

		ServerPlayNetworking.registerGlobalReceiver(ServerboundScreenPeekPayload.TYPE, (payload, context) -> {
			try {
				ServerboundPacket packet = ScreenPeekCodec.decodeServerbound(payload.encoded());
				// The sender id always comes from the connection, never from
				// the packet: clients cannot report activity for someone else.
				switch (packet) {
					case HelloPacket _ -> RELAY.handleHello(context.player().getUUID());
					case ActivityUpdatePacket p -> RELAY.handleActivityUpdate(context.player().getUUID(), p.state());
				}
			} catch (ScreenPeekProtocolException e) {
				handleDecodeFailure(context.player().getUUID(), e);
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, s) -> RELAY.handleDisconnect(handler.player.getUUID()));
	}

	private static void handleDecodeFailure(UUID playerId, ScreenPeekProtocolException e) {
		int received = e.receivedProtocolVersion();
		if (received >= 0) {
			// A readable packet from a client speaking another protocol
			// version: answer with ours so it can adapt or disable itself.
			LOGGER.warn("Player {} speaks ScreenPeek protocol version {} but this server speaks {}",
					playerId, received, ScreenPeekProtocol.PROTOCOL_VERSION);
			TRANSPORT.send(playerId, new WelcomePacket());
		} else {
			// Undecodable bytes from an untrusted source: debug level so a
			// malicious client cannot spam the log.
			LOGGER.debug("Dropped undecodable ScreenPeek packet from {}: {}", playerId, e.getMessage());
		}
	}

	private static final class FabricRelayTransport implements RelayTransport {

		@Override
		public void send(UUID playerId, ClientboundPacket packet) {
			MinecraftServer current = server;
			if (current == null) {
				return;
			}
			ServerPlayer player = current.getPlayerList().getPlayer(playerId);
			if (player != null) {
				ServerPlayNetworking.send(player, new ClientboundScreenPeekPayload(ScreenPeekCodec.encode(packet)));
			}
		}

		@Override
		public void broadcast(UUID excludePlayerId, ClientboundPacket packet) {
			MinecraftServer current = server;
			if (current == null) {
				return;
			}
			byte[] encoded = ScreenPeekCodec.encode(packet);
			for (ServerPlayer player : PlayerLookup.all(current)) {
				if (!player.getUUID().equals(excludePlayerId)) {
					ServerPlayNetworking.send(player, new ClientboundScreenPeekPayload(encoded));
				}
			}
		}
	}
}
