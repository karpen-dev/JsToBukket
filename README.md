## Javascript to bukkit plugin
This plugin allows you to execute javascript files in bukkit plugins.   
Drop you .js files to plugin folder.

This is just a prototype, the code is far from perfect.

**Example code**  
```javascript
function onEnable() {
    logger.info("JS plugin has started");
    
    onEvent("PlayerJoinEvent", (event) => {
        const player = event.getPlayer();
        player.sendMessage(`Welcome to server`);
        player.teleport(player.getLocation().add(0, 5, 0));
    });
}

function onDisable() {
    logger.info("JS plugin shutdown");
}
```
