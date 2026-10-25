package io.github.thermedwolf.screenpeek.protocol.packet;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;

import java.util.Objects;
import java.util.UUID;

/**
 * Another player's activity state, relayed through the server. The relay is
 * expected to stamp its own verified sender id; clients treat the id as
 * authoritative and never trust a claimed identity.
 */
public record PlayerActivityPacket(UUID playerId, ActivityState state) implements ClientboundPacket {

	public PlayerActivityPacket {
		Objects.requireNonNull(playerId, "playerId");
		Objects.requireNonNull(state, "state");
	}
}
