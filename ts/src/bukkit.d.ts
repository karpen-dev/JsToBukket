interface BukkitServer {
    getVersion(): string;
    getOnlinePlayers(): BukkitPlayer[];
    getPluginManager(): BukkitPluginManager;
    getWorld(name: string): BukkitWorld | null;
    broadcast(message: string, permission?: string): void;
}

interface BukkitPluginManager {
    registerEvents(listener: any, plugin: any): void;
}

interface BukkitWorld {
    getName(): string;
    getPlayers(): BukkitPlayer[];
    spawnEntity(location: BukkitLocation, entityType: string): BukkitEntity;
}

interface BukkitEntity {
    getLocation(): BukkitLocation;
    setCustomName(name: string): void;
    remove(): void;
}

interface BukkitPlayer {
    getName(): string;
    getUniqueId(): string;
    getLocation(): BukkitLocation;
    sendMessage(message: string): void;
    hasPermission(permission: string): boolean;
    teleport(location: BukkitLocation): void;
}

interface BukkitLocation {
    getX(): number;
    getY(): number;
    getZ(): number;
    getWorld(): BukkitWorld;
    add(x: number, y: number, z: number): BukkitLocation;
}

interface BukkitEvent {
    getEventName(): string;
    isCancelled(): boolean;
    setCancelled(cancel: boolean): void;
}

interface BukkitLogger {
    info(message: string): void;
    warning(message: string): void;
    severe(message: string): void;
}