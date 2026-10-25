package io.github.thermedwolf.screenpeek.client.render;

import io.github.thermedwolf.screenpeek.client.ClientActivity;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

/**
 * Renders the in-world typing bubble above players who are typing. Chat focus
 * and menu-open states are mutually exclusive screens, so the bubble and the
 * menu panel never occupy the same spot at the same time.
 */
public final class TypingBubbleRenderer {

	/** Height above the feet; vanilla puts the name tag at {@code height + 0.5}. */
	private static final double BUBBLE_HEIGHT = 2.75;

	private static final Component TEXT = Component.literal("typing...").withStyle(ChatFormatting.YELLOW);

	private static List<PeerNameplates.PeerPlate> pending = List.of();

	private TypingBubbleRenderer() {
	}

	public static void register() {
		LevelExtractionEvents.END_EXTRACTION.register(
				context -> pending = PeerNameplates.extract(context, ClientActivity.PEERS.typingPlayers()));
		LevelRenderEvents.COLLECT_SUBMITS.register(
				context -> PeerNameplates.submit(context, pending, BUBBLE_HEIGHT, PeerNameplates.DEFAULT_BACKGROUND, id -> TEXT));
	}
}
