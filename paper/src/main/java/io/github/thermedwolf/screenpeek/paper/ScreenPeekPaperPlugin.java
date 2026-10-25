package io.github.thermedwolf.screenpeek.paper;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper-side relay scaffold. Activity broadcasting is implemented in a later
 * milestone; for now the plugin only proves the build and load pipeline.
 */
public final class ScreenPeekPaperPlugin extends JavaPlugin {

	@Override
	public void onEnable() {
		getLogger().info("ScreenPeek relay enabled");
	}

	@Override
	public void onDisable() {
		getLogger().info("ScreenPeek relay disabled");
	}
}
