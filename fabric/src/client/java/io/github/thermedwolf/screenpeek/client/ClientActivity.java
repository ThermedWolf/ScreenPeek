package io.github.thermedwolf.screenpeek.client;

import io.github.thermedwolf.screenpeek.network.ServerboundScreenPeekPayload;
import io.github.thermedwolf.screenpeek.protocol.ScreenPeekCodec;
import io.github.thermedwolf.screenpeek.protocol.packet.ActivityUpdatePacket;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Static facade over this client's two activity stores: what we know about
 * peers ({@link #PEERS}) and what we report about ourselves
 * ({@link #reporter()}). Both are client-thread only and reset with every
 * session.
 */
public final class ClientActivity {

	/** Other players' latest activity states, as relayed by the server. */
	public static final PeerActivityCache PEERS = new PeerActivityCache();

	private static final ScreenPeekConfig CONFIG = ScreenPeekConfig.load();

	private static final LocalActivityReporter REPORTER = new LocalActivityReporter(
			ClientRelaySession::isRelayPresent, CONFIG::filter, ClientActivity::send);

	private ClientActivity() {
	}

	public static LocalActivityReporter reporter() {
		return REPORTER;
	}

	public static ScreenPeekConfig config() {
		return CONFIG;
	}

	/** Called on join and disconnect; both stores restart with every session. */
	public static void reset() {
		PEERS.clear();
		REPORTER.reset();
	}

	private static void send(ActivityUpdatePacket packet) {
		ClientPlayNetworking.send(new ServerboundScreenPeekPayload(ScreenPeekCodec.encode(packet)));
	}
}
