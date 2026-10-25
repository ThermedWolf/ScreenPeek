package io.github.thermedwolf.screenpeek.protocol;

/**
 * Thrown when a received packet cannot be decoded as ScreenPeek traffic.
 * Unchecked so handlers can stay clean; they are still expected to catch and
 * log it at the network boundary.
 */
public class ScreenPeekProtocolException extends RuntimeException {

	/**
	 * The protocol version parsed from the envelope, or -1 when the envelope
	 * itself was too broken to read a version from. Relays use this to answer
	 * mismatched-version handshakes with their own version.
	 */
	private final int receivedProtocolVersion;

	public ScreenPeekProtocolException(String message) {
		this(message, -1);
	}

	public ScreenPeekProtocolException(String message, int receivedProtocolVersion) {
		super(message);
		this.receivedProtocolVersion = receivedProtocolVersion;
	}

	public int receivedProtocolVersion() {
		return receivedProtocolVersion;
	}
}
