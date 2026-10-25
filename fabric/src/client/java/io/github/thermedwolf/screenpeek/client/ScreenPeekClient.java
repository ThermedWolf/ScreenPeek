package io.github.thermedwolf.screenpeek.client;

import net.fabricmc.api.ClientModInitializer;

public class ScreenPeekClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientNetworking.register();
	}
}
