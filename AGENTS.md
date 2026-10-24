# AGENTS.md

Repo-specific instructions for AI coding sessions working in ScreenPeek.

## Project state

- **ScreenPeek** — Fabric mod for Minecraft 26.x showing other players' activity in multiplayer (typing indicator, open-menu panels, idle markers). Mod id: `screenpeek`.
- Early stage: **docs only** — no build scaffold or source yet. Next milestone: Fabric/Loom project scaffold with original code.
- Scope and roadmap authority: `README.md` (v0.1 = core features only). Keep the README in sync when scope changes.
- The repo is public (`ThermedWolf/ScreenPeek`, branch `main`). Everything not gitignored will be visible — check what you stage.
- No build/test commands exist yet. The `./gradlew` commands in the README are aspirational until the scaffold lands; record real commands here once they do.

## Hard constraints

- `reference/` is **local-only and gitignored** (a fresh clone won't have it). It holds a clone of a third-party ARR-licensed mod, kept strictly for concept study.
  - Never commit or push anything from `reference/`.
  - Never copy code, assets, textures, or names from it into this repo. Studying it to understand *how a feature should behave* is fine; copying its expression is not.
  - The names "WATUT" / "What Are They Up To" must not be used anywhere in this project.
- License: all rights reserved for now. The owner will pick a license before the first release — do not add a `LICENSE` file unilaterally.

## Toolchain targets (verified Oct 2026)

- Minecraft **26.3** (latest stable; requires **Java 25+**)
- Fabric Loader **0.20.1**
- Re-verify at <https://fabricmc.net/develop> or <https://meta.fabricmc.net/v2/versions> if targets move, and update this section.

## Git

- Commit identity is intentionally repo-local: `ThermedWolf` / `253584652+ThermedWolf@users.noreply.github.com`. Do not "fix" it or fall back to the global identity — global `user.name` is misconfigured (contains an email address) and would leak it into this public repo.
- Solo project: commit directly to `main`.
