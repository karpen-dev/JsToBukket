(function() {
    var commandHandlers = {};
    var eventHandlers = {};

    function onEvent(eventName, callback) {
        if (!eventHandlers[eventName]) {
            eventHandlers[eventName] = [];
        }
        eventHandlers[eventName].push(callback);
        plugin.registerEvent(eventName, callback);
    }

    function onCommand(commandName, callback) {
        if (!commandHandlers[commandName]) {
            commandHandlers[commandName] = callback;
            plugin.registerJsCommand(commandName, callback);
        } else {
            throw new Error("Command '" + commandName + "' already registered");
        }
    }

    module.exports = {
        onEvent: onEvent,
        onCommand: onCommand
    };
})();