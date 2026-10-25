package io.github.thermedwolf.screenpeek.protocol;

/**
 * Wire-protocol constants shared by every ScreenPeek implementation so the
 * Fabric mod and the Paper plugin can never disagree on channel names or
 * payload versions.
 */
public final class ScreenPeekProtocol {

	public static final String NAMESPACE = "screenpeek";

	/**
	 * Bump whenever a payload format changes in a way that older clients or
	 * relays cannot read.
	 */
	public static final int PROTOCOL_VERSION = 1;

	private ScreenPeekProtocol() {
	}

	public static String channel(String path) {
		return NAMESPACE + ":" + path;
	}
}
