package io.github.thermedwolf.screenpeek.client;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;
import io.github.thermedwolf.screenpeek.protocol.MenuType;
import io.github.thermedwolf.screenpeek.protocol.packet.ActivityUpdatePacket;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Owns this client's local activity state and reports it to the relay whenever
 * the composed state changes. Detection sources (chat focus, menus, idle) each
 * flip one flag; the reporter does all edge detection, so sources can be naive
 * and the network still only sees real transitions.
 *
 * <p>Pure logic: the relay-presence gate and the transport are injected,
 * keeping this unit-testable without Minecraft.
 */
public final class LocalActivityReporter {

	private final BooleanSupplier relayPresent;
	private final Consumer<ActivityUpdatePacket> sender;

	private boolean typing;
	private MenuType openMenu = MenuType.NONE;
	private boolean idle;
	private ActivityState lastReported = ActivityState.INACTIVE;

	public LocalActivityReporter(BooleanSupplier relayPresent, Consumer<ActivityUpdatePacket> sender) {
		this.relayPresent = Objects.requireNonNull(relayPresent, "relayPresent");
		this.sender = Objects.requireNonNull(sender, "sender");
	}

	public void setTyping(boolean typing) {
		this.typing = typing;
		reportIfChanged();
	}

	public void setOpenMenu(MenuType openMenu) {
		this.openMenu = Objects.requireNonNull(openMenu, "openMenu");
		reportIfChanged();
	}

	public void setIdle(boolean idle) {
		this.idle = idle;
		reportIfChanged();
	}

	/**
	 * Forgets the last reported state (new session). The next change re-sends
	 * even if the composed state is identical to what a previous session saw.
	 */
	public void reset() {
		lastReported = null;
	}

	private void reportIfChanged() {
		ActivityState state = new ActivityState(typing, openMenu, idle);
		if (state.equals(lastReported)) {
			return;
		}
		// Never update lastReported while the relay is absent: when a relay
		// appears, the true state must still go out.
		if (!relayPresent.getAsBoolean()) {
			return;
		}
		sender.accept(new ActivityUpdatePacket(state));
		lastReported = state;
	}
}
