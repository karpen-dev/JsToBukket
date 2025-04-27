/// <reference path="./bukkit.d.ts" />

declare global {
    const server: BukkitServer;
    const plugin: BukkitPlugin;
    const logger: BukkitLogger;

    function onEvent<T extends BukkitEvent>(eventName: string, callback: (event: T) => void): void;

    interface BukkitPlugin {
        getServer(): BukkitServer;
        getLogger(): BukkitLogger;
        getDataFolder(): string;
        reloadScripts(): void;
        registerCommand(command: string, executor: CommandExecutor): void;

        _registerEvent(eventName: string, callback: (event: any) => void): void;
    }

    interface CommandExecutor {
        onCommand(sender: BukkitCommandSender, command: string, args: string[]): boolean;
    }

    interface BukkitCommandSender {
        sendMessage(message: string): void;
        getName(): string;
        isPlayer(): boolean;
        getPlayer(): BukkitPlayer | null;
    }
}

export {};