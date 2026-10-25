package io.github.thermedwolf.screenpeek.client.render;

import io.github.thermedwolf.screenpeek.client.ClientActivity;
import io.github.thermedwolf.screenpeek.client.ClientRelaySession;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Shared mechanism for floating nameplate-style tags above other players:
 * snapshot the peers to draw during level extraction (the only phase allowed
 * to touch the world), then submit vanilla nameplate nodes during
 * COLLECT_SUBMITS. Using vanilla's submitNameTag gives billboarding, distance
 * scaling and occlusion for free.
 */
public final class PeerNameplates {

	/** Same translucent dark plate as a vanilla name tag, slightly stronger. */
	public static final int DEFAULT_BACKGROUND = 0x60000000;

	private PeerNameplates() {
	}

	/**
	 * Extraction phase: resolves the given peer ids to live players and
	 * snapshots what the drawing phase needs (position and light). Peers that
	 * are not in this client's level right now are skipped.
	 */
	public static List<PeerPlate> extract(LevelExtractionContext context, List<UUID> playerIds) {
		if (!ClientRelaySession.isRelayPresent() || playerIds.isEmpty()) {
			return List.of();
		}
		ClientLevel level = context.level();
		Minecraft client = Minecraft.getInstance();
		float partialTick = context.deltaTracker().getGameTimeDeltaPartialTick(false);

		List<PeerPlate> plates = new ArrayList<>();
		for (UUID playerId : playerIds) {
			if (client.player != null && playerId.equals(client.player.getUUID())) {
				continue; // the relay never sends our own state, but stay defensive
			}
			AbstractClientPlayer player = findPlayer(level, playerId);
			if (player == null) {
				continue;
			}
			Vec3 position = player.getPosition(partialTick);
			int light = client.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);
			plates.add(new PeerPlate(playerId, position, light));
		}
		return List.copyOf(plates);
	}

	/**
	 * Submission phase: draws one plate above each snapshotted player's head.
	 * {@code textFor} may read the peer cache — by submission time the state is
	 * already captured, so per-frame text is still stable.
	 */
	public static void submit(
			LevelRenderContext context,
			List<PeerPlate> plates,
			double heightAboveFeet,
			int backgroundColor,
			Function<UUID, Component> textFor) {
		if (plates.isEmpty()) {
			return;
		}
		Vec3 camera = context.levelState().cameraRenderState.pos;
		SubmitNodeCollector collector = context.submitNodeCollector();
		for (PeerPlate plate : plates) {
			context.poseStack().pushPose();
			context.poseStack().translate(
					(float) (plate.position().x - camera.x),
					(float) (plate.position().y - camera.y),
					(float) (plate.position().z - camera.z));
			collector.submitNameTag(
					context.poseStack(),
					new Vec3(0, heightAboveFeet, 0),
					backgroundColor,
					textFor.apply(plate.playerId()),
					false,
					plate.light(),
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

	public record PeerPlate(UUID playerId, Vec3 position, int light) {

		public PeerPlate {
			if (playerId == null) {
				throw new IllegalArgumentException("playerId must not be null");
			}
		}
	}
}
