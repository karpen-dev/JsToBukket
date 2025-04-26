declare global {

    interface Logger {
        info(msg: string): void;
        warning(msg: string): void;
        severe(msg: string): void;
    }

    interface Server {
        getName(): string;
        getVersion(): string;
        getOnlinePlayers(): Player[];
        broadcast(msg: string): void;
    }

    interface PlayerLocation {
        x: number;
        y: number;
        z: number;
        yaw: number;
        pitch: number;
    }

    interface Player {
        getName(): string;
        getDisplayName(): string;
        getUniqueId(): string;
        getAddress(): string | null;
        getWorld(): string;

        getLocation(): PlayerLocation;

        getHealth(): number;
        getLevel(): number;
        getExp(): number;

        isOp(): boolean;
        isOnline(): boolean;
        hasPermission(permission: string): boolean;

        sendMessage(message: string): void;
        sendTitle(title: string, subtitle: string, fadeIn?: number, stay?: number, fadeOut?: number): void;
        playSound(sound: string, volume: number, pitch: number): void;

        kick(reason: string): void;
        teleport(x: number, y: number, z: number, yaw?: number, pitch?: number): void;
        giveItem(material: string, amount: number): void;
    }

    interface Plugin {
        getName(): string;
        getVersion(): string;
        isEnabled(): boolean;
    }

    interface EventManager {
        on(event: 'player.join', handler: (player: Player) => void): void;
        on(event: 'player.quit', handler: (player: Player) => void): void;
    }

    function getLogger(name?: string): Logger;
    function getServer(): Server;
    function getPlugin(): Plugin;
    function getEvents(): EventManager;

    interface MinecraftPlugin {
        onEnable(): void;
        onDisable(): void;
    }
}

export {};