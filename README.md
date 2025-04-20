# EggHunt

## General Information

With the **EggHunt** plugin the players on the server can go on a hunt to find hidden Easter eggs scattered around the world. These Easter eggs will spawn periodically nearby players. Each Easter egg contains a small drop/gift for the player. A leaderboard shows who collected the most Easter eggs. All Easter eggs and drops can be configured in game or via the config.

Placeholders are exposed for the [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI):

- `%egghunt_total_eggs%` (Total Easter eggs found by all players)
- `%egghunt_player_eggs%` (Easter eggs found by a player)

## Configuration

The plugin contains three configuration files: `config.yml`, `drops.yml` and `eggs.yml`; and one statistic file: `statistics.yml`.

Inside the `config.yml` file are the main configurations located. These include the spawning settings for the Easter eggs, leaderboard settings, sounds for special events and valid spawn locations for the Easter eggs.

The available drops from the Easter eggs can be configured inside the `drops.yml` file. A distinction is made between _items_ and _commands_. Each drop has a weighting; higher weight indicates a higher probability. A range can be defined for items as to how many should be dropped. Commands are executed from the server console and may include placeholder like `@p` (player who opened the Easter egg), `@r` (random player) or `@n` (nearest player around the player who opened the Easter egg).

Lastly the items used for the Easter eggs are defined inside `eggs.yml`.

The configuration of the `drops.yml` and `eggs.yml` can also be done using commands.

## Usage

After checking and configuring the main config an administrator with the `egghunt.admin` permission can modify the Easter eggs and drops in game.

The central command for this plugin is `egghunt`. For an overview of all available subcommand use `egghunt help`.

Subcommands:

| Command     | Permission          | Description                                                         |
|-------------|---------------------|---------------------------------------------------------------------|
| drops       | egghunt.configure   | Add drops (items and command) of Easter eggs or remove them via GUI |
| fake        | egghunt.spawn       | Spawn a fake Easter egg near users                                  |
| find        | egghunt.find        | Highlight nearby Easter eggs                                        |
| help        | -                   | Show the help page                                                  |
| leaderboard | egghunt.leaderboard | Show the current leaderboard with most collected Easter eggs        |
| models      | egghunt.configure   | Add/Remove Easter egg models via GUI                                |
| rain        | egghunt.spawn       | Spawn a cloud of Easter eggs above user, which will rain down       |
| reload      | egghunt.configure   | Reload the configuration                                            |
| spawn       | egghunt.spawn       | Spawn an Easter egg near users                                      |
| toggle      | egghunt.toggle      | Toggle the spawning of Easter eggs globally or for specific users   |
