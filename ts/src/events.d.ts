interface PlayerJoinEvent extends BukkitEvent {
    getPlayer(): BukkitPlayer;
    setJoinMessage(message: string): void;
}

interface PlayerQuitEvent extends BukkitEvent {
    getPlayer(): BukkitPlayer;
    setQuitMessage(message: string): void;
}

interface PlayerMoveEvent extends BukkitEvent {
    getPlayer(): BukkitPlayer;
    getFrom(): BukkitLocation;
    getTo(): BukkitLocation;
}

interface BlockBreakEvent extends BukkitEvent {
    getPlayer(): BukkitPlayer;
    getBlock(): BukkitBlock;
}

interface BukkitBlock {
    getType(): string;
    getLocation(): BukkitLocation;
}
