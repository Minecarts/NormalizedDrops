##NormalizedDrops
I wrote this plugin to provide a little method to nerf monster traps on the Minecarts.com PVP server. A sample configuration file is provided below. You can use `/ndrop reload` to reload the configuration file.

**Note:** Reloading plugins with `/reload` will clear the tracking history.


```YAML
debug: false #Log debug messages when normalization occurs

radius: 10 #How close entity events need to be before considered nearby (radius in blocks)
minEvents: 4 #Minimum number of allowed events before normalization
maxEvents: 17 #Any events more than this will cleared of drops, but also the closer this is to min, the rarer drops will be
maxAge: 600 #How long before events expire in seconds

animals: true #Normalize animals
monsters: true #Normalize monsters
```