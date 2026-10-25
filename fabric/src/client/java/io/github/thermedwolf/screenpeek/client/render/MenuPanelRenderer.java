package io.github.thermedwolf.screenpeek.client.render;

import io.github.thermedwolf.screenpeek.client.ClientActivity;
import io.github.thermedwolf.screenpeek.protocol.ActivityState;
import io.github.thermedwolf.screenpeek.protocol.MenuType;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

/**
 * Renders a floating panel above players who have a menu open, labelled and
 * coloured per menu type. Sprite icons per type are planned polish; for v0.1
 * the panel is a coloured vanilla-style nameplate.
 */
public final class MenuPanelRenderer {

	/** Mutually exclusive with the typing bubble, so the same slot is safe. */
	private static final double PANEL_HEIGHT = 2.75;

	private static List<PeerNameplates.PeerPlate> pending = List.of();

	private MenuPanelRenderer() {
	}

	public static void register() {
		LevelExtractionEvents.END_EXTRACTION.register(
				context -> pending = PeerNameplates.extract(context, ClientActivity.PEERS.menuOpenPlayers()));
		LevelRenderEvents.COLLECT_SUBMITS.register(
				context -> PeerNameplates.submit(
						context, pending, PANEL_HEIGHT, PeerNameplates.DEFAULT_BACKGROUND, MenuPanelRenderer::plateFor));
	}

	private static Component plateFor(UUID playerId) {
		MenuType menu = ClientActivity.PEERS.get(playerId)
				.orElse(ActivityState.INACTIVE)
				.openMenu();
		return Component.literal(MenuLabels.text(menu)).withStyle(MenuLabels.color(menu));
	}
}
