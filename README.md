# SkyDimensions

**A NeoForge 1.21.1 addon mod for [Skyblock Builder](https://www.curseforge.com/minecraft/mc-mods/skyblock-builder) that separates the spawn island into its own dedicated dimension.**

Compatible with [Sky GUIs](https://www.curseforge.com/minecraft/mc-mods/sky-guis).

---

## What It Does

In vanilla Skyblock Builder, the spawn island and player team islands all exist in the same overworld dimension. SkyDimensions changes this by:

1. **Registering a new void dimension** (`skydimensions:spawn`) using SB's `skyblockbuilder:noise_based` chunk generator
2. **Redirecting SB's spawn location** to this new dimension, so new/teamless players arrive on a spawn island in a separate world
3. **Keeping team islands in the overworld** — when players create or join a team, SB places their island in the overworld as usual
4. **Applying safe-zone rules** to the spawn dimension (no mobs, no damage, no hunger, etc)

Players use **the same commands** as always — `/skyblock spawn`, `/skyblock home`, `/skyblock create`, etc. If Sky GUIs is installed, using `/sky gui` opens the team GUI exactly as before. The only difference is that the spawn island is in its own clean dimension.

---
## Dependencies

| Mod | Required? | Purpose |
|-----|-----------|---------|
| Skyblock Builder | **Yes** | Core skyblock functionality — island placement, team system, void chunk generator |
| LibX | **Yes** | Required by Skyblock Builder (config system, data management) |
| Sky GUIs | Optional | Team management GUI — all screens work with the spawn dimension |
---

## Configuration

Config file: `config/skydimensions-common.toml`

| Option | Default | Description                                                 |
|--------|---------|-------------------------------------------------------------|
| `enabled` | `true` | Master toggle for the spawn dimension redirect              |
| `useSkyblockBuilderTemplate` | `true` | Place SkyblockBuilder spawn template in the spawn dimension |
| `autoConfigureSkyblockBuilder` | `true` | Automatically redirect Skyblock Builder's spawn teleports to skydimensions:spawn.                |
| `enableSkyGuisCompat` | `true` | Enable Sky GUIs compatibility                               |

### Spawn Protection
Spawn protection is handled by **Skyblock Builder's own config**. Configure in `config/skyblockbuilder/spawn.json5`:

- Set `spawnProtectionRadius` to the number of chunks to protect (default `0` = no protection)
- Customize `spawnProtectionEvents` to control what is blocked (damage, block breaking, mob spawning, etc.)

SkyDimensions extends SB's protection to the spawn dimension automatically.

---
## For Pack Developers

SkyDimensions is designed to work out of the box. The spawn dimension is fully customizable via datapacks. Override the dimension JSON and dimension type JSON in your pack's `data/skydimensions/` folder. For reference on how this works [Skyblock Builder Wiki](https://wiki.chaotictrials.de/docs/1.21.x/wiki/skyblock-builder/packdev/custom-dimensions)

The spawn island template can be customized via Skyblock Builder as shown [here](https://wiki.chaotictrials.de/docs/1.21.x/wiki/skyblock-builder/packdev/create-templates/)

---

## License

MIT License. See LICENSE file.
