package io.github.thermedwolf.screenpeek.client;

import io.github.thermedwolf.screenpeek.client.render.MenuPanelRenderer;
import io.github.thermedwolf.screenpeek.client.render.TypingBubbleRenderer;
import io.github.thermedwolf.screenpeek.client.render.TypingChatHint;

import net.fabricmc.api.ClientModInitializer;

public class ScreenPeekClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientNetworking.register();
		ChatFocusTracker.register();
		OpenMenuTracker.register();
		IdleTracker.register();
		TypingBubbleRenderer.register();
		MenuPanelRenderer.register();
		TypingChatHint.register();
		ScreenPeekCommand.register();
	}
}
