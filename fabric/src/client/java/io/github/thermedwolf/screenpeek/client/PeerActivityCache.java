package io.github.thermedwolf.screenpeek.client;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;
import io.github.thermedwolf.screenpeek.protocol.MenuType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

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

	/** True only when the relay has reported this player as idle. */
	public boolean isIdle(UUID playerId) {
		ActivityState state = states.get(playerId);
		return state != null && state.idle();
	}

	/** Ids of players currently typing; no guaranteed order. */
	public List<UUID> typingPlayers() {
		return matching(ActivityState::typing);
	}

	/** Ids of players with any menu open; no guaranteed order. */
	public List<UUID> menuOpenPlayers() {
		return matching(state -> state.openMenu() != MenuType.NONE);
	}

	private List<UUID> matching(Predicate<ActivityState> filter) {
		return states.entrySet().stream()
				.filter(entry -> filter.test(entry.getValue()))
				.map(Map.Entry::getKey)
				.toList();
	}

	public void clear() {
		states.clear();
	}
}
