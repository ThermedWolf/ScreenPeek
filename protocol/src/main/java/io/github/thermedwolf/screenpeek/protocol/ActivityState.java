package io.github.thermedwolf.screenpeek.protocol;

/**
 * What a player is currently doing, as reported by their own client. Every
 * field is a claim made by that player's client, never inferred by the server.
 *
 * @param typing   true while the player has the chat focused and is typing
 * @param openMenu the menu the player currently has open, or {@link MenuType#NONE}
 * @param idle     true while the player's no-input timeout is exceeded
 */
public record ActivityState(boolean typing, MenuType openMenu, boolean idle) {

	/** The state of a player who is doing nothing reportable. */
	public static final ActivityState INACTIVE = new ActivityState(false, MenuType.NONE, false);

	public ActivityState {
		if (openMenu == null) {
			throw new IllegalArgumentException("openMenu must not be null");
		}
	}
}
