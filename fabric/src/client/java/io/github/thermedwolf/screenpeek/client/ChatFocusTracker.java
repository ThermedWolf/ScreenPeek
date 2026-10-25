package io.github.thermedwolf.screenpeek.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.gui.screens.ChatScreen;

/**
 * Watches chat focus and reports it as the "typing" flag. Focused counts as
 * typing (matching the README's "chat focused / typing" behaviour), so the
 * only events are opening and closing the chat screen — no per-keystroke
 * traffic. Typing also requires the window to be focused: an alt-tabbed
 * player cannot type into Minecraft, so the bubble must not claim they are.
 */
public final class ChatFocusTracker {

	private static boolean chatFocused;

	private ChatFocusTracker() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean focused = client.isWindowActive() && client.gui.screen() instanceof ChatScreen;
			if (focused == chatFocused) {
				return;
			}
			chatFocused = focused;
			ClientActivity.reporter().setTyping(focused);
		});
	}
}
