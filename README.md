## Javascript to bukkit plugin
This plugin allows you to execute javascript files in bukkit plugins.   
Drop you .js files to plugin folder.

This is just a prototype, the code is far from perfect.

**Example code**  
```javascript
(function() {
    const plugin = require('plugin');

    plugin.onEvent("PlayerJoinEvent", function(event) {
        const player = event.getPlayer();
        player.sendMessage("Welcome to server!");
        player.teleport(player.getLocation().add(0, 5, 0));
    });

    plugin.onCommand("testjs", function(cmd) {
        cmd.sender.sendMessage("JS command works!");
        return true;
    });

    function onEnable() {
        plugin.logger.info("Script enabled");
    }

    function onDisable() {
        plugin.logger.info("Script disabled");
    }
})();
```
