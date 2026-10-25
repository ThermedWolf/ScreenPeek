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
- **Idle detection** — losing the game window (another window/tab, or a
  minimized window) marks players idle, surfaced in the tab player list.
- **Privacy first** — every broadcast category is a client-side opt-out. You
  decide exactly what leaves your client: `/screenpeek <typing|menu|idle>
  <on|off>` applies instantly and persists to `config/screenpeek.json`;
  opted-out data is zeroed before it is sent, never just hidden.

### Server support

The features need a server-side relay that passes activity between players.
ScreenPeek ships that relay twice, on one shared wire protocol:

- **Fabric servers** — built into the mod itself (v0.1).
- **Paper servers** — a companion Paper plugin for servers that cannot run
  Fabric mods (v0.2). Players still need the Fabric client mod; vanilla
  players simply share nothing and see nothing.

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
| Paper (companion server plugin, v0.2) | 26.3 |
| Java (build & runtime) | 25+ |

## Inspiration & originality

This project exists because we loved the *idea* of showing what players are up
to in multiplayer (as seen in mods like Corosauce's WATUT) and wanted an
independent implementation on modern Fabric. To be explicit:

- **No code, assets, or names from any other project are used here.**
- Everything in this repository is an original implementation of the concept.
- This project is not affiliated with, endorsed by, or derived from any other mod.

## Development

The repository is a Gradle multi-project build with one shared protocol
module:

```
protocol/   wire-protocol constants and payload types shared by every
            implementation, so the Fabric mod and Paper plugin can never
            disagree on the wire format
fabric/     the Fabric mod (client + built-in server relay), split into
            main and client source sets
paper/      the companion Paper plugin (relay, v0.2)
```

### Prerequisites

- JDK 25 or newer — check with `java -version`. The Gradle daemon itself must
  also run on Java 25+; a stale `org.gradle.java.home` in a global
  `~/.gradle/gradle.properties` fails the build with
  "Dependency requires at least JVM runtime version 25".
- Git

### Build

```bash
./gradlew build   # compiles and packages all modules
```

Expected artifacts:

- `fabric/build/libs/screenpeek-<version>.jar` — the Fabric mod (plus a `-sources` jar)
- `paper/build/libs/screenpeek-paper-<version>.jar` — the Paper plugin
- `protocol/build/libs/protocol-<version>.jar` — shared protocol library (bundled into the other two)

### Verify

```bash
./gradlew check   # tests and validation, run in CI on every push
```

No tests exist yet; `check` currently covers compilation and packaging.

#### In-game verification (two players)

Dev runs use offline accounts, so a local dedicated server plus two clients
covers every feature:

```bash
./gradlew :fabric:runServer    # wait for "Done ..." (localhost, offline mode)
./gradlew :fabric:runClient    # client 1: join localhost
./gradlew :fabric:runClient2   # client 2: join localhost (username Player2)
```

Checklist:

| Action | Expected on the other client |
| --- | --- |
| Client 1 opens chat | "typing..." bubble above their head + "Player2 is typing..." hint |
| Client 2 presses E / opens a chest | panel labelled "inventory" / "chest" above their head |
| Client 2 alt-tabs or minimizes | their tab-list name turns grey and italic; typing bubble (if any) clears |
| `/screenpeek menu off` on client 2 | panel disappears for everyone; `/screenpeek` shows `menu=off` |
| Client 2 closes the world | their tag disappears everywhere |

## License

**All rights reserved** for now. A definitive open-source license will be chosen
before the first public release.
