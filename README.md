# Bindle <img height="70" src="https://github.com/TerminatorNL/Bindle/blob/master/logo.svg">
A [Sponge](https://www.spongepowered.org/) plugin capable of transferring items between servers.

## Features
* Transferring items between servers using SQL
  * This includes all their NBT data!
* Transferring player NBT between servers using SQL
* User-friendly interface (Chest-like GUI with buttons)
* Lightweight

<img src="https://github.com/TerminatorNL/Bindle/blob/master/GUI.png">

## Dependencies
[SpongeForge](https://www.spongepowered.org/), [Forge](http://www.minecraftforge.net/)

## Commands
`/bindle` (`bindle.user.use`)<br>
Opens your Bindle

`/bindle 4` (`bindle.user.use`)<br>
Opens your Bindle on page 4

`/bindle put-self` (`bindle.user.putself`)<br>
Uploads your player.dat to the SQL server

`/bindle get-self` (`bindle.user.getself`)<br>
Retrieves your player.dat from the SQL server

`/bindleReload` (`bindle.admin.reload`)<br>
Reloads the Bindle config, useful for setting up SQL.
