# AGENTS.md

Repo-specific instructions for AI coding sessions working in ScreenPeek.

## Project state

- **ScreenPeek** — Fabric mod for Minecraft 26.x showing other players' activity in multiplayer (typing indicator, open-menu panels, idle markers). Mod id: `screenpeek`.
- Early stage: monorepo scaffold landed (2026-10-25) — Gradle multi-project
  build with `protocol/` (shared wire-protocol constants/types), `fabric/`
  (the mod, split main/client source sets, Mojang mappings), and `paper/`
  (companion Paper relay plugin, stub only). `./gradlew build` verified green.
- Protocol v1 landed (2026-10-25): `ActivityState`/`MenuType` + sealed `packet/`
  hierarchy (`Hello`/`ActivityUpdate` serverbound, `Welcome`/`PlayerActivity`
  clientbound), self-contained byte[] `ScreenPeekCodec` with a version-stamped
  envelope. Fabric transport wraps opaque bytes in payloads on
  `screenpeek:serverbound`/`screenpeek:clientbound` (size-capped at 256 B,
  registered via `PayloadTypeRegistry` in common init).
- Server relay landed (2026-10-25): transport-agnostic `protocol/relay/`
  (`ScreenPeekRelay` + `RelayTransport`) — Hello→Welcome plus a tracked-state
  snapshot, full-state updates broadcast to everyone except the sender,
  disconnect broadcasts INACTIVE, reset on server stop. Fabric wiring lives in
  `relay/RelayNetworking` (volatile server holder + Fabric transport); the
  client announces via `client/ClientNetworking` and tracks relay presence in
  `client/ClientRelaySession`. 18 JUnit tests total. The Paper plugin (v0.2)
  will reuse `ScreenPeekRelay` with a Bukkit transport.
- Typing indicator landed (2026-10-25): `client/ChatFocusTracker` (tick-based
  chat-focus edges; focused counts as typing), `client/LocalActivityReporter`
  (composed state, change-only sends, relay-gated; pure logic, 5 unit tests),
  `client/PeerActivityCache` (peer states from snapshot + updates),
  `client/render/TypingBubbleRenderer` (in-world bubble reusing vanilla
  `submitNameTag` via END_EXTRACTION snapshot + COLLECT_SUBMITS) and
  `client/render/TypingChatHint` ("X is typing..." hint attached before the
  vanilla chat HUD layer). Client-only unit tests run via a test source set
  that also sees client output.
- Menu peeking landed (2026-10-25): `client/OpenMenuTracker` (tick-based
  screen→MenuType mapping: INVENTORY, CHEST (ContainerScreen family),
  CRAFTING, FURNACE, other `AbstractContainerScreen` → GENERIC),
  `client/render/PeerNameplates` (shared snapshot+submit mechanism for
  vanilla-style tags), refactored `TypingBubbleRenderer` onto it, and
  `client/render/MenuPanelRenderer` + `MenuLabels` (per-type coloured label;
  sprite icons are later polish). 33 JUnit tests total.
- Idle detection landed (2026-10-25) with window-based semantics (user
  decision, README synced): idle = the Minecraft window is not active
  (minimized or another window/tab has focus — GLFW collapses both into one
  signal, `Minecraft.isWindowActive()`). `client/IdleTracker` reports it;
  `client/ChatFocusTracker` additionally gates typing on window focus (an
  alt-tabbed player cannot type). Tab-list marker is the project's first
  mixin: `mixin/client/PlayerTabOverlayMixin` styles idle players'
  `getNameForDisplay` result grey+italic (registered in
  `screenpeek.client.mixins.json`). authlib 10 note: GameProfile is a record
  (`id()`, not `getId()`). 34 JUnit tests total.
- Privacy config landed (2026-10-25) — v0.1 features are code-complete:
  `client/ScreenPeekConfig` (`config/screenpeek.json`, Gson, log-and-defaults
  on parse errors), enforced at composition via a `UnaryOperator<ActivityState>`
  privacy filter in `LocalActivityReporter` (opted-out fields are zeroed before
  sending, never just hidden), `/screenpeek` client command (status +
  `typing|menu|idle on|off`, applies live via `reporter.refresh()`), 35 JUnit
  tests total. Second dev client configured: `:fabric:runClient2`
  (`run2/` dir, username Player2) for two-player verification against
  `:fabric:runServer` — checklist in README "Verify"; visual placement
  constants still need human eyes before any release.
- Next milestone: in-game verification pass, then v0.1 release prep — the
  owner must pick a license before the first public release (see Hard
  constraints).
- Scope and roadmap authority: `README.md` (v0.1 = Fabric client + built-in
  server relay with core features; v0.2 = Paper relay plugin). Paper support
  decision (2026-10-25): companion plugin, monorepo, shared `protocol`
  module. Keep the README in sync when scope changes.
- The repo is public (`ThermedWolf/ScreenPeek`, branch `main`). Everything not gitignored will be visible — check what you stage.
- Build (verified): `./gradlew build` produces `fabric/build/libs/screenpeek-<version>.jar`,
  `paper/build/libs/screenpeek-paper-<version>.jar`, and
  `protocol/build/libs/protocol-<version>.jar`. No tests yet — `./gradlew check`
  is the entry point once the first tests land.

## Hard constraints

- `reference/` is **local-only and gitignored** (a fresh clone won't have it). It holds a clone of a third-party ARR-licensed mod, kept strictly for concept study.
  - Never commit or push anything from `reference/`.
  - Never copy code, assets, textures, or names from it into this repo. Studying it to understand *how a feature should behave* is fine; copying its expression is not.
  - The names "WATUT" / "What Are They Up To" must not be used anywhere in this project.
- License: all rights reserved for now. The owner will pick a license before the first release — do not add a `LICENSE` file unilaterally.

## Toolchain targets (verified 2026-10-25)

- Minecraft **26.3** (latest stable; requires **Java 25+**)
- Fabric Loader **0.20.1**, Fabric API **0.165.0+26.3**
- Fabric Loom **1.19-SNAPSHOT** on the Gradle 9.8.2 wrapper; Loom requires the
  **Gradle daemon on Java 25+** (a stale `org.gradle.java.home` in a global
  `~/.gradle/gradle.properties` will fail the build)
- Mappings: **Mojang official** — Yarn is no longer published for Minecraft
  26.x; the build declares no `mappings` line (matches fabric-example-mod master)
- Paper plugin (v0.2): `paper-api` **26.3.build.222-stable**, `plugin.yml`
  `api-version: '26.3'`; protocol classes must be shaded into the plugin jar
  once it does real work
- Re-verify at <https://fabricmc.net/develop>, <https://meta.fabricmc.net/v2/versions>, and <https://repo.papermc.io> (paper-api metadata) if targets move, and update this section.

## Git

- Commit identity is intentionally repo-local: `ThermedWolf` / `253584652+ThermedWolf@users.noreply.github.com`. Do not "fix" it or fall back to the global identity — global `user.name` is misconfigured (contains an email address) and would leak it into this public repo.
- Solo project: commit directly to `main`.
