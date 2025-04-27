/// <reference path="./bukkit.d.ts" />
/// <reference path="./plugin.d.ts" />
/// <reference path="./events.d.ts" />

declare module 'bukkit' {
    export const server: BukkitServer;
    export const plugin: BukkitPlugin;
    export const logger: BukkitLogger;

    export function onEvent<T extends BukkitEvent>(eventName: string, callback: (event: T) => void): void;
    export function scheduleTask(delay: number, callback: () => void): void;
    export function scheduleRepeatingTask(delay: number, period: number, callback: () => void): void;
}