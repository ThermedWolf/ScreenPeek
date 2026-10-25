package io.github.thermedwolf.screenpeek.protocol.relay;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;
import io.github.thermedwolf.screenpeek.protocol.MenuType;
import io.github.thermedwolf.screenpeek.protocol.packet.ClientboundPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.PlayerActivityPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.WelcomePacket;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ScreenPeekRelayTest {

	private static final UUID ALICE =
			UUID.nameUUIDFromBytes("relay-test-alice".getBytes(StandardCharsets.UTF_8));
	private static final UUID BOB =
			UUID.nameUUIDFromBytes("relay-test-bob".getBytes(StandardCharsets.UTF_8));

	private final RecordingTransport transport = new RecordingTransport();
	private final ScreenPeekRelay relay = new ScreenPeekRelay(transport);

	@Test
	void helloGetsWelcomeThenSnapshot() {
		relay.handleActivityUpdate(ALICE, new ActivityState(false, MenuType.CHEST, false));
		transport.clear();

		relay.handleHello(BOB);

		assertEquals(2, transport.sends.size());
		assertInstanceOf(WelcomePacket.class, transport.sends.get(0).packet());

		PlayerActivityPacket snapshot =
				assertInstanceOf(PlayerActivityPacket.class, transport.sends.get(1).packet());
		assertEquals(ALICE, snapshot.playerId());
		assertEquals(MenuType.CHEST, snapshot.state().openMenu());
	}

	@Test
	void helloDropsStaleOwnState() {
		relay.handleActivityUpdate(BOB, new ActivityState(true, MenuType.NONE, false));
		transport.clear();

		relay.handleHello(BOB); // re-announce on a fresh connection

		assertEquals(1, transport.sends.size());
		assertInstanceOf(WelcomePacket.class, transport.sends.get(0).packet());
	}

	@Test
	void updateIsStoredAndBroadcastExcludingSender() {
		relay.handleActivityUpdate(ALICE, new ActivityState(true, MenuType.INVENTORY, false));

		assertEquals(1, transport.broadcasts.size());
		RecordingTransport.Broadcast broadcast = transport.broadcasts.get(0);
		assertEquals(ALICE, broadcast.excludedPlayerId());

		PlayerActivityPacket packet = assertInstanceOf(PlayerActivityPacket.class, broadcast.packet());
		assertEquals(ALICE, packet.playerId());
		assertEquals(MenuType.INVENTORY, packet.state().openMenu());
	}

	@Test
	void latestUpdateWins() {
		relay.handleActivityUpdate(ALICE, new ActivityState(true, MenuType.CHEST, false));
		relay.handleActivityUpdate(ALICE, ActivityState.INACTIVE);
		transport.clear();

		relay.handleHello(BOB);

		PlayerActivityPacket snapshot =
				(PlayerActivityPacket) transport.sends.get(1).packet();
		assertEquals(ActivityState.INACTIVE, snapshot.state());
	}

	@Test
	void disconnectClearsAndBroadcastsInactive() {
		relay.handleActivityUpdate(ALICE, new ActivityState(true, MenuType.CHEST, false));
		transport.clear();

		relay.handleDisconnect(ALICE);

		assertEquals(1, transport.broadcasts.size());
		PlayerActivityPacket packet =
				(PlayerActivityPacket) transport.broadcasts.get(0).packet();
		assertEquals(ALICE, packet.playerId());
		assertEquals(ActivityState.INACTIVE, packet.state());
	}

	@Test
	void disconnectOfUnknownPlayerIsSilent() {
		relay.handleDisconnect(ALICE);

		assertEquals(0, transport.broadcasts.size());
		assertEquals(0, transport.sends.size());
	}

	@Test
	void resetForgetsEverything() {
		relay.handleActivityUpdate(ALICE, new ActivityState(true, MenuType.CHEST, false));
		relay.reset();
		transport.clear();

		relay.handleHello(BOB);

		assertEquals(1, transport.sends.size()); // welcome only, no snapshot
	}

	private static final class RecordingTransport implements RelayTransport {

		record Sent(UUID to, ClientboundPacket packet) {
		}

		record Broadcast(UUID excludedPlayerId, ClientboundPacket packet) {
		}

		final List<Sent> sends = new ArrayList<>();
		final List<Broadcast> broadcasts = new ArrayList<>();

		void clear() {
			sends.clear();
			broadcasts.clear();
		}

		@Override
		public void send(UUID playerId, ClientboundPacket packet) {
			sends.add(new Sent(playerId, packet));
		}

		@Override
		public void broadcast(UUID excludePlayerId, ClientboundPacket packet) {
			broadcasts.add(new Broadcast(excludePlayerId, packet));
		}
	}
}
