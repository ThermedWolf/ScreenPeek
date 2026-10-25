package io.github.thermedwolf.screenpeek;

import io.github.thermedwolf.screenpeek.network.ScreenPeekNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenPeek implements ModInitializer {

	public static final String MOD_ID = "screenpeek";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ScreenPeekNetworking.register();
		LOGGER.info("ScreenPeek initialized");
	}

	/**
	 * Namespaced identifier helper so every channel and resource this mod
	 * registers shares a single namespace.
	 */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
