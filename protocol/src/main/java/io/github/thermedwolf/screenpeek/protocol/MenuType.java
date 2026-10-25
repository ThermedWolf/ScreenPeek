package io.github.thermedwolf.screenpeek.protocol;

/**
 * Menus that ScreenPeek can distinguish when peeking at another player's
 * screen. Wire ids are stable; never renumber an existing entry.
 *
 * <p>{@link #GENERIC} doubles as the fallback for ids a client does not
 * recognise (a newer protocol may add menus an older client cannot name), so
 * decoding never fails on unknown menus — it degrades to "some menu".
 */
public enum MenuType {

	NONE(0),
	INVENTORY(1),
	CHEST(2),
	CRAFTING(3),
	FURNACE(4),
	GENERIC(255);

	private final int wireId;

	MenuType(int wireId) {
		this.wireId = wireId;
	}

	public int wireId() {
		return wireId;
	}

	/**
	 * Resolves a wire id to a menu type. Unknown ids resolve to
	 * {@link #GENERIC} instead of failing, keeping old clients compatible with
	 * future menu additions.
	 */
	public static MenuType fromWire(int wireId) {
		for (MenuType type : values()) {
			if (type.wireId == wireId) {
				return type;
			}
		}
		return GENERIC;
	}
}
