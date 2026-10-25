package io.github.thermedwolf.screenpeek.protocol;

import io.github.thermedwolf.screenpeek.protocol.packet.ActivityUpdatePacket;
import io.github.thermedwolf.screenpeek.protocol.packet.ClientboundPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.HelloPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.PlayerActivityPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.ScreenPeekPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.ServerboundPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.WelcomePacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

/**
 * The one authoritative ScreenPeek wire format.
 *
 * <p>Envelope layout: one byte protocol version, one byte packet type, then
 * the packet body. The version byte lets a relay recognise (and politely
 * answer) clients speaking another protocol version instead of crashing on
 * them.
 *
 * <p>Encoding is fully self-contained to plain {@code byte[]}, so every
 * transport (Fabric payload codec, Paper plugin messaging) moves opaque bytes
 * and no transport ever re-implements the format.
 */
public final class ScreenPeekCodec {

	// Type ids within the serverbound channel; never renumber.
	static final int TYPE_HELLO = 0;
	static final int TYPE_ACTIVITY_UPDATE = 1;

	// Type ids within the clientbound channel; ids may overlap between channels.
	static final int TYPE_WELCOME = 0;
	static final int TYPE_PLAYER_ACTIVITY = 1;

	private ScreenPeekCodec() {
	}

	/** Serialises any packet; the envelope is stamped with this build's protocol version. */
	public static byte[] encode(ScreenPeekPacket packet) {
		return switch (packet) {
			case HelloPacket _ -> envelope(TYPE_HELLO);
			case ActivityUpdatePacket p -> envelope(TYPE_ACTIVITY_UPDATE, out -> writeActivityState(out, p.state()));
			case WelcomePacket _ -> envelope(TYPE_WELCOME);
			case PlayerActivityPacket p -> envelope(TYPE_PLAYER_ACTIVITY, out -> {
				writeUuid(out, p.playerId());
				writeActivityState(out, p.state());
			});
		};
	}

	public static ServerboundPacket decodeServerbound(byte[] data) throws ScreenPeekProtocolException {
		DataInputStream in = open(data);
		int version = readVersion(in);
		int type = readType(in, version);
		ServerboundPacket packet = switch (type) {
			case TYPE_HELLO -> new HelloPacket();
			case TYPE_ACTIVITY_UPDATE -> new ActivityUpdatePacket(readActivityState(in, version));
			default -> throw new ScreenPeekProtocolException("Unknown serverbound packet type " + type, version);
		};
		ensureFullyConsumed(in, version);
		return packet;
	}

	public static ClientboundPacket decodeClientbound(byte[] data) throws ScreenPeekProtocolException {
		DataInputStream in = open(data);
		int version = readVersion(in);
		int type = readType(in, version);
		ClientboundPacket packet = switch (type) {
			case TYPE_WELCOME -> new WelcomePacket();
			case TYPE_PLAYER_ACTIVITY -> {
				UUID playerId = readUuid(in, version);
				yield new PlayerActivityPacket(playerId, readActivityState(in, version));
			}
			default -> throw new ScreenPeekProtocolException("Unknown clientbound packet type " + type, version);
		};
		ensureFullyConsumed(in, version);
		return packet;
	}

	private static byte[] envelope(int type) {
		return envelope(type, out -> {
		});
	}

	private static byte[] envelope(int type, BodyWriter body) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(bytes)) {
			out.writeByte(ScreenPeekProtocol.PROTOCOL_VERSION);
			out.writeByte(type);
			body.write(out);
		} catch (IOException e) {
			// ByteArrayOutputStream never throws; reaching this means a JDK bug.
			throw new UncheckedIOException("In-memory encoding failed", e);
		}
		return bytes.toByteArray();
	}

	private static DataInputStream open(byte[] data) {
		return new DataInputStream(new ByteArrayInputStream(data));
	}

	private static int readVersion(DataInputStream in) throws ScreenPeekProtocolException {
		int version = requireByte(in, "protocol version", -1);
		if (version != ScreenPeekProtocol.PROTOCOL_VERSION) {
			throw new ScreenPeekProtocolException(
					"Unsupported protocol version " + version + ", expected "
							+ ScreenPeekProtocol.PROTOCOL_VERSION,
					version);
		}
		return version;
	}

	private static int readType(DataInputStream in, int version) throws ScreenPeekProtocolException {
		return requireByte(in, "packet type", version);
	}

	private static ActivityState readActivityState(DataInputStream in, int version) throws ScreenPeekProtocolException {
		boolean typing = requireByte(in, "typing flag", version) != 0;
		MenuType openMenu = MenuType.fromWire(requireByte(in, "open menu id", version));
		boolean idle = requireByte(in, "idle flag", version) != 0;
		return new ActivityState(typing, openMenu, idle);
	}

	private static void writeActivityState(DataOutputStream out, ActivityState state) throws IOException {
		out.writeBoolean(state.typing());
		out.writeByte(state.openMenu().wireId());
		out.writeBoolean(state.idle());
	}

	private static void writeUuid(DataOutputStream out, UUID id) throws IOException {
		out.writeLong(id.getMostSignificantBits());
		out.writeLong(id.getLeastSignificantBits());
	}

	private static UUID readUuid(DataInputStream in, int version) throws ScreenPeekProtocolException {
		long mostSignificant = requireLong(in, "player id (msb)", version);
		long leastSignificant = requireLong(in, "player id (lsb)", version);
		return new UUID(mostSignificant, leastSignificant);
	}

	private static int requireByte(DataInputStream in, String what, int version) throws ScreenPeekProtocolException {
		try {
			return in.readUnsignedByte();
		} catch (IOException e) {
			throw truncated(what, version);
		}
	}

	private static long requireLong(DataInputStream in, String what, int version) throws ScreenPeekProtocolException {
		try {
			return in.readLong();
		} catch (IOException e) {
			throw truncated(what, version);
		}
	}

	private static ScreenPeekProtocolException truncated(String what, int version) {
		return new ScreenPeekProtocolException("Truncated packet: missing " + what, version);
	}

	private static void ensureFullyConsumed(DataInputStream in, int version) throws ScreenPeekProtocolException {
		try {
			if (in.read() != -1) {
				throw new ScreenPeekProtocolException("Trailing bytes after packet body", version);
			}
		} catch (IOException e) {
			throw new ScreenPeekProtocolException("Could not verify packet end", version);
		}
	}

	@FunctionalInterface
	private interface BodyWriter {
		void write(DataOutputStream out) throws IOException;
	}
}
