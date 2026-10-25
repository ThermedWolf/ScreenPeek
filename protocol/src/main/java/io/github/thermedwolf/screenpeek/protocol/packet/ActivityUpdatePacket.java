package io.github.thermedwolf.screenpeek.protocol.packet;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;

import java.util.Objects;

/**
 * The sending player's full activity state. Each update replaces whatever
 * state was reported before, so dropped packets only ever delay an update
 * instead of corrupting it.
 */
public record ActivityUpdatePacket(ActivityState state) implements ServerboundPacket {

	public ActivityUpdatePacket {
		Objects.requireNonNull(state, "state");
	}
}
