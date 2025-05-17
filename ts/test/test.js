function onEnable() {
    logger.info("JS plugin has started");

    onEvent("PlayerJoinEvent", (event) => {
        const player = event.getPlayer();
        player.sendMessage(`Welcome to server`);
        player.teleport(player.getLocation().add(0, 5, 0));
    });

    onCommand("testjs", function(command) {
        var sender = command.sender;
        var args = command.args;

        sender.sendMessage("Test from js");

        return true;
    });
}

function onDisable() {
    logger.info("JS plugin shutdown");
}