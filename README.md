# SopAfterworld

SopAfterworld is a Bukkit/Spigot plugin that adds an afterworld-style death flow with corpse recovery, portal guidance, and configurable respawn radius scaling.

## Features

- Configurable afterworld world and portal search area
- Built-in custom afterworld terrain generator
- Hilly hell-style landscape with lava lowlands
- Generator settings for seed, height, lava level, and terrain scales
- Corpse spawning through SopLib's shared corpse service
- Recoverable player inventory from corpses
- Requirement checks through optional PlaceholderAPI and permissions
- Shared text and item helpers through SopLib
- Built-in afterworld regeneration command with confirmation

## Requirements

- Java 8+
- Spigot/Paper 1.16.5
- SopLib
- PlaceholderAPI (optional)
- Vault (optional, for built-in balance fallback)

## Commands

- `/sopafterworld`
- `/sopafterworld reload`
- `/sopafterworld regenerate`
- `/portal`

## Notes

- SopLib is required as a separate plugin dependency.
- The plugin keeps old `totalafterworld` compatibility as an alias.
- The custom generator only affects newly generated chunks.
- Use `/sopafterworld regenerate` twice within 15 seconds to rebuild the afterworld with the current generator settings.
