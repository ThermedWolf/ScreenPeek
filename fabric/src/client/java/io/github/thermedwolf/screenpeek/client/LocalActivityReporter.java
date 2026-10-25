package io.github.thermedwolf.screenpeek.client;

import io.github.thermedwolf.screenpeek.protocol.ActivityState;
import io.github.thermedwolf.screenpeek.protocol.MenuType;
import io.github.thermedwolf.screenpeek.protocol.packet.ActivityUpdatePacket;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Owns this client's local activity state and reports it to the relay whenever
 * the composed state changes. Detection sources (chat focus, menus, idle) each
 * flip one flag; the reporter does all edge detection, so sources can be naive
 * and the network only sees real transitions.
 *
 * <p>Before an update leaves the client it passes through the injected
 * {@code privacyFilter}, which zeroes every opted-out category — privacy is
 * enforced at composition, not at display. Pure logic: the relay-presence
 * gate, the filter and the transport are all injected, keeping this
 * unit-testable without Minecraft.
 */
public final class LocalActivityReporter {

	private final BooleanSupplier relayPresent;
	private final UnaryOperator<ActivityState> privacyFilter;
	private final Consumer<ActivityUpdatePacket> sender;

	private boolean typing;
	private MenuType openMenu = MenuType.NONE;
	private boolean idle;
	private ActivityState lastReported = ActivityState.INACTIVE;

	public LocalActivityReporter(
			BooleanSupplier relayPresent,
			UnaryOperator<ActivityState> privacyFilter,
			Consumer<ActivityUpdatePacket> sender) {
		this.relayPresent = Objects.requireNonNull(relayPresent, "relayPresent");
		this.privacyFilter = Objects.requireNonNull(privacyFilter, "privacyFilter");
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
	 * Re-reports the current state even without a detection edge — used when
	 * the privacy filter changes, so an opt-out takes effect immediately.
	 */
	public void refresh() {
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
		ActivityState outgoing = privacyFilter.apply(new ActivityState(typing, openMenu, idle));
		if (outgoing.equals(lastReported)) {
			return;
		}
		// Never update lastReported while the relay is absent: when a relay
		// appears, the true state must still go out.
		if (!relayPresent.getAsBoolean()) {
			return;
		}
		sender.accept(new ActivityUpdatePacket(outgoing));
		lastReported = outgoing;
	}
}
