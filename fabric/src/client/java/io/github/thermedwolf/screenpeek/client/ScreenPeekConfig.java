package io.github.thermedwolf.screenpeek.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import io.github.thermedwolf.screenpeek.ScreenPeek;
import io.github.thermedwolf.screenpeek.protocol.ActivityState;
import io.github.thermedwolf.screenpeek.protocol.MenuType;

import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Client-side privacy configuration: every broadcast category can be opted
 * out of, and opted-out data never leaves the client — it is zeroed before an
 * update is composed, not merely hidden from other screens.
 *
 * <p>Stored at {@code config/screenpeek.json}; toggled live via
 * {@code /screenpeek <category> <on|off>}.
 */
public final class ScreenPeekConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScreenPeek.MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private boolean broadcastTyping = true;
	private boolean broadcastMenu = true;
	private boolean broadcastIdle = true;

	public static ScreenPeekConfig load() {
		Path path = path();
		if (!Files.exists(path)) {
			ScreenPeekConfig defaults = new ScreenPeekConfig();
			defaults.save();
			return defaults;
		}
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			ScreenPeekConfig config = GSON.fromJson(reader, ScreenPeekConfig.class);
			return config != null ? config : new ScreenPeekConfig();
		} catch (IOException | JsonParseException e) {
			LOGGER.warn("Could not read {} ({}); using defaults", path, e.toString());
			return new ScreenPeekConfig();
		}
	}

	public void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(path())) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			LOGGER.warn("Could not save {}", path(), e);
		}
	}

	/** Zeroes every opted-out category before the state leaves the client. */
	public ActivityState filter(ActivityState state) {
		return new ActivityState(
				broadcastTyping && state.typing(),
				broadcastMenu ? state.openMenu() : MenuType.NONE,
				broadcastIdle && state.idle());
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("screenpeek.json");
	}

	public boolean broadcastTyping() {
		return broadcastTyping;
	}

	public boolean broadcastMenu() {
		return broadcastMenu;
	}

	public boolean broadcastIdle() {
		return broadcastIdle;
	}

	public void setBroadcastTyping(boolean value) {
		broadcastTyping = value;
	}

	public void setBroadcastMenu(boolean value) {
		broadcastMenu = value;
	}

	public void setBroadcastIdle(boolean value) {
		broadcastIdle = value;
	}
}
