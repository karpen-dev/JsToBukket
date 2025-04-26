class Simpleplugin implements MinecraftPlugin {
    private logger = getLogger("TS");

    onEnable() {
        this.logger.info("Hi form ts");
    }

    onDisable() {
        this.logger.info("Goodbye from ts");
    }
}

export default new Simpleplugin();