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

The available drop can be viewed via `egghunt drops`. With the opened inventory the player can get a drop (left-click the item) or remove a drop (drop the item).
Additional drops can be added with `egghunt drops add item <Minimum> <Maximum> <Weight>` or `egghunt drops add command <Command> <Weight>`. For adding an item, hold it in the main hand.

Similarly, Easter eggs can be viewed and removed with `egghunt models`. Adding a new item can be done inside the overview inventory by clicking on the desired item in the main inventory.

To test the spawning of Easter eggs, the `egghunt spawn` and `egghunt find` command can be used to spawn a new Easter egg and find it afterward (Permission `egghunt.admin`).
In addition, the `egghunt rain` command can spawn a cloud of eggs, which will fall down, but may break (Permission `egghunt.admin`).
With the `egghunt fake` command, an explosive egg can be spawned (Permission `egghunt.admin`).

This plugin allows for the modification of the configuration files without a server restart. To reload the configurations use `egghunt reload` (Permission `egghunt.admin`).

The `egghunt toggle` command can be used to disable the spawning of Easter eggs temporarily for a player or globally (Permission `egghunt.admin`).

A leaderboard with the most collected Easter eggs can be viewed with `egghunt leaderboard` (Permission `egghunt.leaderboard`).
