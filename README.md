# ScreenPeek

> See what everyone on your server is up to — right in the world.

**ScreenPeek** is a Fabric mod for Minecraft 26.x that makes other players'
current activity visible at a glance: who is typing in chat, who is buried in a
chest menu, and who quietly wandered off AFK.

**Status:** 🚧 in active early development — no release yet. Watch this repo.

## Concept

In vanilla multiplayer there is no way to tell whether your friend is deep in a
menu, halfway through writing a message, or simply gone. ScreenPeek detects
activity on each player's client, shares it through the server, and renders it
for everyone else:

| Activity | In-world | On your screen |
| --- | --- | --- |
| Chat focused / typing | small typing bubble above the head | "X is typing…" hint on the chat screen |
| Menu open (inventory, chest, crafting, furnace, …) | floating panel with an icon of that menu in front of the player | — |
| Idle / AFK | — | idle marker in the tab player list |

### Planned core features (v0.1)

- **Typing indicator** — an in-world bubble plus a Discord-style hint line on
  the chat screen, with configurable text.
- **Menu peeking** — a small floating panel in front of each player showing
  which menu they have open, with a distinct icon per menu type.
- **Idle detection** — a configurable no-input timeout marks players idle,
  surfaced in the tab player list.
- **Privacy first** — every broadcast category is a client-side opt-out. You
  decide exactly what leaves your client.

### Later ideas

- Richer body animations (head looking down at the screen, click gestures)
- Subtle sounds when nearby players open menus or click in them
- Item particles showing container transfers
- Opt-in live menu sharing (a mini snapshot of the actual screen instead of an icon)

## Target platform

| Component | Version |
| --- | --- |
| Minecraft | 26.3 (latest stable) |
| Loader | Fabric |
| Java (build & runtime) | 25+ |

## Inspiration & originality

This project exists because we loved the *idea* of showing what players are up
to in multiplayer (as seen in mods like Corosauce's WATUT) and wanted an
independent implementation on modern Fabric. To be explicit:

- **No code, assets, or names from any other project are used here.**
- Everything in this repository is an original implementation of the concept.
- This project is not affiliated with, endorsed by, or derived from any other mod.

## Development

The code scaffold is landing next. Once it does, the workflow will be:

### Prerequisites

- JDK 25 or newer — check with `java -version`
- Git

### Build

```bash
./gradlew build   # compiles and packages the mod
```

Expected result: `build/libs/screenpeek-<version>.jar`

### Verify

```bash
./gradlew check   # tests and validation, run in CI on every push
```

## License

**All rights reserved** for now. A definitive open-source license will be chosen
before the first public release.
