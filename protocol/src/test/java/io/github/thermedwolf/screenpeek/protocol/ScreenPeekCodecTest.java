package io.github.thermedwolf.screenpeek.protocol;

import io.github.thermedwolf.screenpeek.protocol.packet.ActivityUpdatePacket;
import io.github.thermedwolf.screenpeek.protocol.packet.ClientboundPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.HelloPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.PlayerActivityPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.ServerboundPacket;
import io.github.thermedwolf.screenpeek.protocol.packet.WelcomePacket;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenPeekCodecTest {

	private static final UUID PLAYER_ID =
			UUID.nameUUIDFromBytes("screenpeek-codec-test".getBytes(StandardCharsets.UTF_8));

	@Test
	void helloRoundTrips() {
		ServerboundPacket decoded = ScreenPeekCodec.decodeServerbound(ScreenPeekCodec.encode(new HelloPacket()));

		assertTrue(decoded instanceof HelloPacket);
	}

	@Test
	void welcomeRoundTrips() {
		ClientboundPacket decoded = ScreenPeekCodec.decodeClientbound(ScreenPeekCodec.encode(new WelcomePacket()));

		assertTrue(decoded instanceof WelcomePacket);
	}

	@Test
	void activityUpdateRoundTrips() {
		ActivityState state = new ActivityState(true, MenuType.CHEST, false);

		ActivityUpdatePacket decoded =
				(ActivityUpdatePacket) ScreenPeekCodec.decodeServerbound(ScreenPeekCodec.encode(new ActivityUpdatePacket(state)));

		assertEquals(state, decoded.state());
	}

	@Test
	void playerActivityRoundTrips() {
		ActivityState state = new ActivityState(false, MenuType.INVENTORY, true);

		PlayerActivityPacket decoded = (PlayerActivityPacket) ScreenPeekCodec.decodeClientbound(
				ScreenPeekCodec.encode(new PlayerActivityPacket(PLAYER_ID, state)));

		assertEquals(PLAYER_ID, decoded.playerId());
		assertEquals(state, decoded.state());
	}

	@Test
	void unknownMenuIdDecodesToGeneric() {
		byte[] raw = raw(out -> {
			out.writeByte(ScreenPeekProtocol.PROTOCOL_VERSION);
			out.writeByte(ScreenPeekCodec.TYPE_ACTIVITY_UPDATE);
			out.writeBoolean(true);
			out.writeByte(99); // menu id a v1 client never sent
			out.writeBoolean(false);
		});

		ActivityUpdatePacket decoded = (ActivityUpdatePacket) ScreenPeekCodec.decodeServerbound(raw);

		assertEquals(MenuType.GENERIC, decoded.state().openMenu());
	}

	@Test
	void wrongVersionIsRejected() {
		byte[] raw = raw(out -> {
			out.writeByte(ScreenPeekProtocol.PROTOCOL_VERSION + 1);
			out.writeByte(ScreenPeekCodec.TYPE_HELLO);
		});

		ScreenPeekProtocolException e = assertThrows(
				ScreenPeekProtocolException.class, () -> ScreenPeekCodec.decodeServerbound(raw));

		assertEquals(ScreenPeekProtocol.PROTOCOL_VERSION + 1, e.receivedProtocolVersion());
	}

	@Test
	void unknownTypeIsRejected() {
		byte[] raw = raw(out -> {
			out.writeByte(ScreenPeekProtocol.PROTOCOL_VERSION);
			out.writeByte(42);
		});

		assertThrows(ScreenPeekProtocolException.class, () -> ScreenPeekCodec.decodeServerbound(raw));
	}

	@Test
	void truncatedPacketIsRejected() {
		byte[] encoded = ScreenPeekCodec.encode(new ActivityUpdatePacket(new ActivityState(true, MenuType.CHEST, true)));
		byte[] truncated = new byte[encoded.length - 1];

		System.arraycopy(encoded, 0, truncated, 0, truncated.length);

		assertThrows(ScreenPeekProtocolException.class, () -> ScreenPeekCodec.decodeServerbound(truncated));
	}

	@Test
	void trailingGarbageIsRejected() {
		byte[] encoded = ScreenPeekCodec.encode(new HelloPacket());
		byte[] padded = new byte[encoded.length + 1];

		System.arraycopy(encoded, 0, padded, 0, encoded.length);
		padded[encoded.length] = 0x42;

		assertThrows(ScreenPeekProtocolException.class, () -> ScreenPeekCodec.decodeServerbound(padded));
	}

	private static byte[] raw(RawWriter writer) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(bytes)) {
			writer.write(out);
		} catch (IOException e) {
			throw new UncheckedIOException("In-memory test encoding failed", e);
		}
		return bytes.toByteArray();
	}

	@FunctionalInterface
	private interface RawWriter {
		void write(DataOutputStream out) throws IOException;
	}
}
