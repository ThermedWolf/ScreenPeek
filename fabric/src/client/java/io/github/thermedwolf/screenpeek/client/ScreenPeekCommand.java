package io.github.thermedwolf.screenpeek.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * {@code /screenpeek} — in-game privacy controls. Bare command shows status;
 * {@code /screenpeek <typing|menu|idle> <on|off>} toggles a broadcast
 * category. Changes apply immediately (the reporter re-sends the filtered
 * state) and are persisted to config/screenpeek.json.
 */
public final class ScreenPeekCommand {

	private ScreenPeekCommand() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommands.literal("screenpeek")
						.executes(ctx -> {
							feedbackStatus(ctx.getSource());
							return Command.SINGLE_SUCCESS;
						})
						.then(category("typing",
								ScreenPeekConfig::setBroadcastTyping, ScreenPeekConfig::broadcastTyping))
						.then(category("menu",
								ScreenPeekConfig::setBroadcastMenu, ScreenPeekConfig::broadcastMenu))
						.then(category("idle",
								ScreenPeekConfig::setBroadcastIdle, ScreenPeekConfig::broadcastIdle))));
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> category(
			String name,
			BiConsumer<ScreenPeekConfig, Boolean> setter,
			Function<ScreenPeekConfig, Boolean> getter) {
		return ClientCommands.literal(name)
				.then(ClientCommands.literal("on").executes(ctx -> {
					apply(ctx.getSource(), name, setter, true);
					return Command.SINGLE_SUCCESS;
				}))
				.then(ClientCommands.literal("off").executes(ctx -> {
					apply(ctx.getSource(), name, setter, false);
					return Command.SINGLE_SUCCESS;
				}))
				.executes(ctx -> {
					ctx.getSource().sendFeedback(Component.literal(
							"ScreenPeek " + name + ": " + onOff(getter.apply(ClientActivity.config()))));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static void apply(
			FabricClientCommandSource source,
			String name,
			BiConsumer<ScreenPeekConfig, Boolean> setter,
			boolean value) {
		setter.accept(ClientActivity.config(), value);
		ClientActivity.config().save();
		ClientActivity.reporter().refresh();
		source.sendFeedback(Component.literal(
				"ScreenPeek now broadcasting " + name + ": " + onOff(value)));
	}

	private static void feedbackStatus(FabricClientCommandSource source) {
		ScreenPeekConfig config = ClientActivity.config();
		source.sendFeedback(Component.literal(
				"ScreenPeek: typing=" + onOff(config.broadcastTyping())
						+ " menu=" + onOff(config.broadcastMenu())
						+ " idle=" + onOff(config.broadcastIdle())
						+ " | relay: " + (ClientRelaySession.isRelayPresent() ? "present" : "absent")));
	}

	private static String onOff(boolean value) {
		return value ? "on" : "off";
	}
}
