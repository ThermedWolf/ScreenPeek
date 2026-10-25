package io.github.thermedwolf.screenpeek.client;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;
import io.github.thermedwolf.screenpeek.protocol.MenuType;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeerActivityCacheTest {

	private static final UUID ALICE =
			UUID.nameUUIDFromBytes("cache-test-alice".getBytes());
	private static final UUID BOB =
			UUID.nameUUIDFromBytes("cache-test-bob".getBytes());

	private final PeerActivityCache cache = new PeerActivityCache();

	@Test
	void typingAndMenuOpenPlayersAreFilteredIndependently() {
		cache.update(ALICE, new ActivityState(true, MenuType.NONE, false));
		cache.update(BOB, new ActivityState(false, MenuType.CHEST, false));

		assertEquals(List.of(ALICE), cache.typingPlayers());
		assertEquals(List.of(BOB), cache.menuOpenPlayers());
	}

	@Test
	void bothFlagsOnOnePlayerAppearInBothLists() {
		cache.update(ALICE, new ActivityState(true, MenuType.FURNACE, false));

		assertEquals(List.of(ALICE), cache.typingPlayers());
		assertEquals(List.of(ALICE), cache.menuOpenPlayers());
	}

	@Test
	void inactivePlayersAppearInNeitherList() {
		cache.update(ALICE, ActivityState.INACTIVE);

		assertEquals(List.of(), cache.typingPlayers());
		assertEquals(List.of(), cache.menuOpenPlayers());
	}

	@Test
	void updateReplacesPreviousState() {
		cache.update(ALICE, new ActivityState(false, MenuType.CHEST, false));
		cache.update(ALICE, ActivityState.INACTIVE);

		assertEquals(List.of(), cache.menuOpenPlayers());
	}

	@Test
	void isIdleIsTrueOnlyForIdlePlayers() {
		cache.update(ALICE, new ActivityState(false, MenuType.NONE, true));
		cache.update(BOB, ActivityState.INACTIVE);

		assertEquals(true, cache.isIdle(ALICE));
		assertEquals(false, cache.isIdle(BOB));
		assertEquals(false, cache.isIdle(UUID.nameUUIDFromBytes("unknown".getBytes())));
	}
}
