# Changelog

## 2.0

### Added

- Spectator-style smooth camera acceleration and deceleration, enabled by default.
- Temporary mouse-wheel speed adjustment from `0.25x` to `8x`.
- Settings for mouse-wheel speed control, food, drinks, elytra rockets, and inventory actions.
- A Reset to defaults button in the config screen.

### Changed

- The real player now remains under normal world physics while freecam is active.
- Food, drinks, and elytra rockets are allowed by default; item drops and offhand swaps are blocked by default.
- Pick-block and pick-entity actions are blocked while freecam is active.
- Chunk culling now follows spectator behavior: optimized in open space and disabled while the camera is inside a solid block.
- Fabric Loader 0.19.2 or newer is now required.

### Fixed

- Crouching is preserved when freecam is enabled.
- Eating and drinking work while freecam is active.
- Elytra flight continues without stuttering, including firework boosts.
- Water currents, knockback, gravity, and other external forces continue moving the real player.
- Disable-on-damage detects damage absorbed by absorption hearts.
- Config files are saved atomically, and malformed configs are backed up instead of crashing the game.
- Removed a fragile renderer injection and fixed the Java 25 Mixin compatibility warning.
- Reduced the FPS impact of freecam without hiding underground caves.
