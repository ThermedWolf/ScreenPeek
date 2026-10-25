package io.github.thermedwolf.screenpeek.protocol.relay;

import io.github.thermedwolf.screenpeek.protocol.packet.ClientboundPacket;

import java.util.UUID;

/**
 * Delivery seam between {@link ScreenPeekRelay} logic and a concrete
 * transport. The Fabric mod and the Paper plugin each implement this against
 * their own networking APIs; the relay logic itself never touches either.
 */
public interface RelayTransport {

	/**
	 * Delivers a packet to one specific player. Implementations must silently
	 * ignore players that are no longer online.
	 */
	void send(UUID playerId, ClientboundPacket packet);

	/**
	 * Delivers a packet to every online player except {@code excludePlayerId},
	 * which is the player whose action caused the packet.
	 */
	void broadcast(UUID excludePlayerId, ClientboundPacket packet);
}
