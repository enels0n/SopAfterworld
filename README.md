# SopAfterworld

SopAfterworld is a Bukkit/Spigot plugin that adds an afterworld-style death flow with corpse recovery, portal guidance, and configurable respawn radius scaling.

## Features

- Configurable afterworld world and portal search area
- Built-in custom afterworld terrain generator
- Hilly hell-style landscape with lava lowlands
- Generator settings for seed, height, lava level, and terrain scales
- Corpse spawning through Citizens
- Recoverable player inventory from corpses
- Requirement checks through PlaceholderAPI and permissions
- Shared text and item helpers through SopLib

## Requirements

- Java 8+
- Spigot/Paper 1.20.4
- SopLib
- PlaceholderAPI
- Citizens

## Commands

- `/sopafterworld`
- `/sopafterworld reload`
- `/portal`

## Notes

- SopLib is required as a separate plugin dependency.
- The plugin keeps old `totalafterworld` compatibility as an alias.
- The custom generator only affects newly generated chunks.
- If you previously used a pre-generated `afterworld`, delete or rename that world folder before first startup to fully regenerate it with the new terrain.
