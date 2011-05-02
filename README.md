##NormalizedDrops
I wrote this plugin to provide a little method to nerf monster traps on the Minecarts.com PVP server. A sample configuration file is provided below. You can use /ndrop reload to reload the configuration file. Note: Reloading the plugins will clear the tracking history of nearby deaths.


minDeaths: 7 #Minimum number of allowed deaths before loot normalization
maxDeaths: 15 #Any deaths more than this will be 0 drops, but also the closer this is to minDeaths, the rarer loot will be
timeFactor: 600 #How long before deaths expire in seconds
radius: 10 #How close entity deaths need to be before considered nearby (radius in blocks)
debug: true #Log debug messages when loot is normalized
