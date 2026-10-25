package io.github.thermedwolf.screenpeek.client;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Everything this client knows about other players' activity, filled by the
 * relay (welcome snapshot plus live updates). Client-thread only — all Fabric
 * client networking handlers and render events run on the render thread, so
 * the map is deliberately unsynchronized.
 */
public final class PeerActivityCache {

	private final Map<UUID, ActivityState> states = new HashMap<>();

	public void update(UUID playerId, ActivityState state) {
		states.put(playerId, state);
	}

	public Optional<ActivityState> get(UUID playerId) {
		return Optional.ofNullable(states.get(playerId));
	}

	/** Ids of players currently typing; no guaranteed order. */
	public List<UUID> typingPlayers() {
		return states.entrySet().stream()
				.filter(entry -> entry.getValue().typing())
				.map(Map.Entry::getKey)
				.toList();
	}

	public void clear() {
		states.clear();
	}
}
