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