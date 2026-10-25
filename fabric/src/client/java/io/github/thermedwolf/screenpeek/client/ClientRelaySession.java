package io.github.thermedwolf.screenpeek.client;

/**
 * Client-side session state for the relay connection. Features consult
 * {@link #isRelayPresent()} before reporting or rendering anything, so a
 * server without a ScreenPeek relay never receives ScreenPeek traffic.
 */
public final class ClientRelaySession {

	private static volatile boolean relayPresent;

	private ClientRelaySession() {
	}

	public static boolean isRelayPresent() {
		return relayPresent;
	}

	static void markPresent() {
		relayPresent = true;
	}

	static void reset() {
		relayPresent = false;
	}
}
