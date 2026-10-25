package io.github.thermedwolf.screenpeek.protocol.relay;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;
import io.github.thermedwolf.screenpeek.protocol.packet.ClientboundPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.PlayerActivityPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.WelcomePacket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Transport-agnostic relay logic: the single source of truth for what a
 * ScreenPeek server does with client traffic. Both the Fabric mod and the
 * Paper plugin drive this class and only supply a {@link RelayTransport}.
 *
 * <p>Threading contract: every method must be called on the server thread —
 * the Fabric wiring guarantees this because all its entry points (receivers,
 * connection events, lifecycle events) fire there. The state map is therefore
 * deliberately unsynchronized.
 */
public final class ScreenPeekRelay {

	private final Map<UUID, ActivityState> activityByPlayer = new LinkedHashMap<>();
	private final RelayTransport transport;

	public ScreenPeekRelay(RelayTransport transport) {
		this.transport = Objects.requireNonNull(transport, "transport");
	}

	/**
	 * A player announced ScreenPeek: welcome them, then bring them up to date
	 * with a snapshot of every tracked state. Without the snapshot, a late
	 * joiner would stay blind to ongoing activity until each player's next
	 * change.
	 */
	public void handleHello(UUID playerId) {
		// A Hello always starts a fresh session; drop stale state from a
		// previous connection of the same player before snapshotting.
		activityByPlayer.remove(playerId);
		transport.send(playerId, new WelcomePacket());
		activityByPlayer.forEach((id, state) ->
				transport.send(playerId, new PlayerActivityPacket(id, state)));
	}

	/**
	 * A player reported their full activity state: store it and inform everyone
	 * else. The player id must come from the connection, never from the packet.
	 */
	public void handleActivityUpdate(UUID playerId, ActivityState state) {
		Objects.requireNonNull(state, "state");
		activityByPlayer.put(playerId, state);
		transport.broadcast(playerId, new PlayerActivityPacket(playerId, state));
	}

	/**
	 * A player left: drop their entry and, if they had reported any state,
	 * broadcast an inactive update so clients clear their view. Players that
	 * never sent traffic are silent — nobody was ever told anything about them.
	 */
	public void handleDisconnect(UUID playerId) {
		ActivityState removed = activityByPlayer.remove(playerId);
		if (removed != null) {
			transport.broadcast(playerId, new PlayerActivityPacket(playerId, ActivityState.INACTIVE));
		}
	}

	/** Forgets all tracked state (server stop); players re-announce via Hello. */
	public void reset() {
		activityByPlayer.clear();
	}
}
