# AGENTS.md

Repo-specific instructions for AI coding sessions working in ScreenPeek.

## Project state

- **ScreenPeek** — Fabric mod for Minecraft 26.x showing other players' activity in multiplayer (typing indicator, open-menu panels, idle markers). Mod id: `screenpeek`.
- Early stage: monorepo scaffold landed (2026-10-25) — Gradle multi-project
  build with `protocol/` (shared wire-protocol constants/types), `fabric/`
  (the mod, split main/client source sets, Mojang mappings), and `paper/`
  (companion Paper relay plugin, stub only). `./gradlew build` verified green.
- Next milestone: activity data model + network payloads in `protocol/`,
  then the Fabric server relay and client features.
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
