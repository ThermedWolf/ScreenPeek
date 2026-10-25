package io.github.thermedwolf.screenpeek.client;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;
import io.github.thermedwolf.screenpeek.protocol.MenuType;
import io.github.thermedwolf.screenpeek.protocol.packet.ActivityUpdatePacket;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalActivityReporterTest {

	@Test
	void sendsOnlyOnChange() {
		List<ActivityUpdatePacket> sent = new ArrayList<>();
		LocalActivityReporter reporter = reporter(true, UnaryOperator.identity(), sent);

		reporter.setTyping(true);
		reporter.setTyping(true); // duplicate edge: no traffic
		reporter.setTyping(false);

		assertEquals(2, sent.size());
		assertEquals(true, sent.get(0).state().typing());
		assertEquals(false, sent.get(1).state().typing());
	}

	@Test
	void composesAllFlagsIntoFullState() {
		List<ActivityUpdatePacket> sent = new ArrayList<>();
		LocalActivityReporter reporter = reporter(true, UnaryOperator.identity(), sent);

		reporter.setTyping(true);
		reporter.setOpenMenu(MenuType.CHEST);

		assertEquals(2, sent.size());
		ActivityUpdatePacket last = sent.get(1);
		assertEquals(true, last.state().typing());
		assertEquals(MenuType.CHEST, last.state().openMenu());
		assertEquals(false, last.state().idle());
	}

	@Test
	void silentWhileRelayAbsentThenSendsFullState() {
		List<ActivityUpdatePacket> sent = new ArrayList<>();
		boolean[] relayPresent = {false};
		LocalActivityReporter reporter =
				new LocalActivityReporter(() -> relayPresent[0], UnaryOperator.identity(), sent::add);

		reporter.setTyping(true);
		assertEquals(0, sent.size());

		relayPresent[0] = true;
		reporter.setIdle(true);

		assertEquals(1, sent.size());
		assertEquals(true, sent.get(0).state().typing()); // full state, not a delta
		assertEquals(true, sent.get(0).state().idle());
	}

	@Test
	void resetResendsIdenticalState() {
		List<ActivityUpdatePacket> sent = new ArrayList<>();
		LocalActivityReporter reporter = reporter(true, UnaryOperator.identity(), sent);

		reporter.setTyping(true);
		reporter.setTyping(false);
		assertEquals(2, sent.size());

		reporter.reset(); // new session, relay saw nothing of the old one
		reporter.setTyping(false);

		assertEquals(3, sent.size());
	}

	@Test
	void optedOutCategoriesNeverLeaveTheClient() {
		List<ActivityUpdatePacket> sent = new ArrayList<>();
		// typing and menu opted out: the outgoing state can never contain them
		LocalActivityReporter reporter = reporter(true,
				state -> new ActivityState(false, MenuType.NONE, state.idle()), sent);

		reporter.setTyping(true);
		reporter.setOpenMenu(MenuType.CHEST);

		// the filtered state equals the last reported one, so nothing is sent
		assertEquals(0, sent.size());
	}

	@Test
	void configChangeResendsFilteredState() {
		List<ActivityUpdatePacket> sent = new ArrayList<>();
		boolean[] shareTyping = {true};
		LocalActivityReporter reporter = reporter(true,
				state -> new ActivityState(shareTyping[0] && state.typing(), state.openMenu(), state.idle()),
				sent);

		reporter.setTyping(true);
		assertEquals(1, sent.size());
		assertEquals(true, sent.get(0).state().typing());

		shareTyping[0] = false; // "/screenpeek typing off"
		reporter.refresh();

		assertEquals(2, sent.size());
		assertEquals(false, sent.get(1).state().typing());
	}

	private static LocalActivityReporter reporter(
			boolean relayPresent,
			UnaryOperator<io.github.thermedwolf.screenpeek.protocol.ActivityState> filter,
			List<ActivityUpdatePacket> sent) {
		return new LocalActivityReporter(() -> relayPresent, filter, sent::add);
	}
}
