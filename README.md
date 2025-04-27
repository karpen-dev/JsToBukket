## Javascript to bukkit plugin
This plugin allows you to execute javascript files in bukkit plugins.   
Drop you .js files to plugin folder.

**Example code**  
Typescript:
```typescript
declare const plugin: any;
declare const logger: any;
declare function onEvent(eventName: string, callback: (event: any) => void): void;

interface PlayerEvent {
    getPlayer(): {
        getName(): string;
        sendMessage(message: string): void;
        teleport(location: any): void;
    };
}

interface PlayerJoinEvent extends PlayerEvent {
    setJoinMessage(message: string): void;
}

interface PlayerQuitEvent extends PlayerEvent {
    setQuitMessage(message: string): void;
}

function onEnable() {
    logger.info("SimplePlugin enabled!");

    onEvent("PlayerJoinEvent", (event: PlayerJoinEvent) => {
        const player = event.getPlayer();
        player.sendMessage("Welcome to server!");
    });

}

function onDisable() {
    logger.info("SimplePlugin disabled!");
}
```
Javascript:
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