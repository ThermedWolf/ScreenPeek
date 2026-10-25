package io.github.thermedwolf.screenpeek.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Watches the window state and reports the idle flag. Per the agreed
 * semantics, idle means the player is not looking at Minecraft: the window is
 * minimised or another window/tab has focus. A minimised window always loses
 * focus, so one check covers both.
 */
public final class IdleTracker {

	private static boolean idle;

	private IdleTracker() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean nowIdle = !client.isWindowActive();
			if (nowIdle == idle) {
				return;
			}
			idle = nowIdle;
			ClientActivity.reporter().setIdle(nowIdle);
		});
	}
}
