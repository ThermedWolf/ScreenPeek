package io.github.thermedwolf.screenpeek.client.render;

import io.github.thermedwolf.screenpeek.client.ClientActivity;
import io.github.thermedwolf.screenpeek.client.ClientRelaySession;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Renders the in-world typing bubble above players who are typing. Vanilla's
 * own name-tag submission path is reused ({@code submitNameTag}), so the
 * bubble billboards, scales and occludes exactly like a nameplate.
 *
 * <p>The 26.x rendering split is respected: {@code END_EXTRACTION} touches the
 * world and snapshots the players to draw; {@code COLLECT_SUBMITS} only reads
 * that snapshot and submits render nodes.
 */
public final class TypingBubbleRenderer {

	/**
	 * Height of the attachment point above the player's feet. Vanilla puts the
	 * name tag at {@code height + 0.5}; the bubble floats just above it.
	 */
	private static final double BUBBLE_HEIGHT = 2.75;

	/** Same translucent dark plate as a vanilla name tag, slightly stronger. */
	private static final int BACKGROUND_COLOR = 0x60000000;

	private static List<Snapshot> pending = List.of();

	private TypingBubbleRenderer() {
	}

	public static void register() {
		LevelExtractionEvents.END_EXTRACTION.register(TypingBubbleRenderer::extract);
		LevelRenderEvents.COLLECT_SUBMITS.register(TypingBubbleRenderer::collectSubmits);
	}

	private static void extract(LevelExtractionContext context) {
		if (!ClientRelaySession.isRelayPresent()) {
			pending = List.of();
			return;
		}
		ClientLevel level = context.level();
		Minecraft client = Minecraft.getInstance();
		float partialTick = context.deltaTracker().getGameTimeDeltaPartialTick(false);

		pending = ClientActivity.PEERS.typingPlayers().stream()
				.map(id -> snapshot(level, client, id, partialTick))
				.filter(Objects::nonNull)
				.toList();
	}

	private static Snapshot snapshot(ClientLevel level, Minecraft client, UUID playerId, float partialTick) {
		if (client.player != null && playerId.equals(client.player.getUUID())) {
			return null; // the relay never sends our own state, but stay defensive
		}
		AbstractClientPlayer player = findPlayer(level, playerId);
		if (player == null) {
			return null;
		}
		Vec3 position = player.getPosition(partialTick);
		int light = client.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);
		return new Snapshot(position, light);
	}

	private static void collectSubmits(LevelRenderContext context) {
		if (pending.isEmpty()) {
			return;
		}
		Vec3 camera = context.levelState().cameraRenderState.pos;
		SubmitNodeCollector collector = context.submitNodeCollector();
		for (Snapshot snapshot : pending) {
			context.poseStack().pushPose();
			context.poseStack().translate(
					(float) (snapshot.position().x - camera.x),
					(float) (snapshot.position().y - camera.y),
					(float) (snapshot.position().z - camera.z));
			collector.submitNameTag(
					context.poseStack(),
					new Vec3(0, BUBBLE_HEIGHT, 0),
					BACKGROUND_COLOR,
					TEXT,
					false,
					snapshot.light(),
					context.levelState().cameraRenderState);
			context.poseStack().popPose();
		}
	}

	private static AbstractClientPlayer findPlayer(ClientLevel level, UUID playerId) {
		for (AbstractClientPlayer player : level.players()) {
			if (player.getUUID().equals(playerId)) {
				return player;
			}
		}
		return null;
	}

	private static final Component TEXT = Component.literal("typing...").withStyle(ChatFormatting.YELLOW);

	private record Snapshot(Vec3 position, int light) {
	}
}
