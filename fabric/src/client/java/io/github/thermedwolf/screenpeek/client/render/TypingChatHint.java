package io.github.thermedwolf.screenpeek.client.render;

import io.github.thermedwolf.screenpeek.ScreenPeek;
import io.github.thermedwolf.screenpeek.client.ClientActivity;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Discord-style "X is typing..." hint on the chat HUD. Attached directly
 * before vanilla's chat layer so it sits with the chat elements.
 */
public final class TypingChatHint {

	private static final int MAX_NAMES = 3;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	/** Roughly one text line above the chat's bottom anchor. */
	private static final int BOTTOM_MARGIN = 30;
	/** Extra lift while the input box occupies the chat's home position. */
	private static final int CHAT_OPEN_EXTRA_LIFT = 14;

	private TypingChatHint() {
	}

	public static void register() {
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, ScreenPeek.id("typing_hint"), TypingChatHint::render);
	}

	private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			return;
		}
		List<String> names = typingNames(client.level);
		if (names.isEmpty()) {
			return;
		}
		String hint = buildHint(names);
		int y = graphics.guiHeight() - BOTTOM_MARGIN;
		if (client.gui.screen() instanceof ChatScreen) {
			y -= CHAT_OPEN_EXTRA_LIFT;
		}
		graphics.text(client.font, hint, 4, y, TEXT_COLOR);
	}

	private static List<String> typingNames(ClientLevel level) {
		return ClientActivity.PEERS.typingPlayers().stream()
				.map(id -> nameOf(level, id))
				.filter(Objects::nonNull)
				.limit(MAX_NAMES)
				.toList();
	}

	private static String nameOf(ClientLevel level, UUID playerId) {
		for (AbstractClientPlayer player : level.players()) {
			if (player.getUUID().equals(playerId)) {
				return player.getName().getString();
			}
		}
		return null;
	}

	/** Pure text building; visible for tests. */
	static String buildHint(List<String> names) {
		int hidden = names.size() - MAX_NAMES;
		if (hidden > 0) {
			names = names.subList(0, MAX_NAMES);
		}
		String joined = String.join(", ", names.subList(0, names.size() - 1));
		String last = names.get(names.size() - 1);
		String subject = joined.isEmpty() ? last : joined + " and " + last;
		if (hidden > 0) {
			subject += " and " + hidden + " more";
		}
		boolean singular = names.size() == 1;
		return subject + (singular ? " is typing..." : " are typing...");
	}
}
