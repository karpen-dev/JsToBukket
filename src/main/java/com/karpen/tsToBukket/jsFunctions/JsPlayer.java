package com.karpen.tsToBukket.jsFunctions;

import com.karpen.tsToBukket.TsToBukket;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.Arrays;
import java.util.HashMap;

public class JsPlayer implements ProxyObject {
    private final TsToBukket plugin;
    private final Player bukkitPlayer;

    public JsPlayer(TsToBukket plugin, Player bukkitPlayer) {
        this.plugin = plugin;
        this.bukkitPlayer = bukkitPlayer;
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    @Override
    public Object getMember(String key) {
        switch (key) {
            case "getName":
                return (ProxyExecutable) arguments -> bukkitPlayer.getName();

            case "getDisplayName":
                return (ProxyExecutable) arguments -> bukkitPlayer.getDisplayName();

            case "getUniqueId":
                return (ProxyExecutable) arguments -> bukkitPlayer.getUniqueId().toString();

            case "getAddress":
                return (ProxyExecutable) arguments -> bukkitPlayer.getAddress() != null
                        ? bukkitPlayer.getAddress().getAddress().toString()
                        : null;

            case "getWorld":
                return (ProxyExecutable) arguments -> bukkitPlayer.getWorld().getName();

            case "getLocation":
                return (ProxyExecutable) arguments -> {
                    return ProxyObject.fromMap(new HashMap<String, Object>() {{
                        put("x", bukkitPlayer.getLocation().getX());
                        put("y", bukkitPlayer.getLocation().getY());
                        put("z", bukkitPlayer.getLocation().getZ());
                        put("yaw", bukkitPlayer.getLocation().getYaw());
                        put("pitch", bukkitPlayer.getLocation().getPitch());
                    }});
                };

            case "getHealth":
                return (ProxyExecutable) arguments -> bukkitPlayer.getHealth();

            case "getLevel":
                return (ProxyExecutable) arguments -> bukkitPlayer.getLevel();

            case "getExp":
                return (ProxyExecutable) arguments -> bukkitPlayer.getExp();

            case "isOp":
                return (ProxyExecutable) arguments -> bukkitPlayer.isOp();

            case "isOnline":
                return (ProxyExecutable) arguments -> bukkitPlayer.isOnline();

            case "hasPermission":
                return (ProxyExecutable) arguments -> {
                    if (arguments.length < 1) return false;
                    return bukkitPlayer.hasPermission(arguments[0].asString());
                };

            case "sendMessage":
                return (ProxyExecutable) arguments -> {
                    if (arguments.length < 1) return null;
                    bukkitPlayer.sendMessage(arguments[0].asString());
                    return null;
                };

            case "sendTitle":
                return (ProxyExecutable) arguments -> {
                    if (arguments.length < 2) return null;
                    bukkitPlayer.sendTitle(
                            arguments[0].asString(), // title
                            arguments[1].asString(), // subtitle
                            arguments.length > 2 ? arguments[2].asInt() : 10, // fadeIn
                            arguments.length > 3 ? arguments[3].asInt() : 70, // stay
                            arguments.length > 4 ? arguments[4].asInt() : 20 // fadeOut
                    );
                    return null;
                };

            case "playSound":
                return (ProxyExecutable) arguments -> {
                    if (arguments.length < 3) return null;
                    bukkitPlayer.playSound(
                            bukkitPlayer.getLocation(),
                            arguments[0].asString(), // sound
                            arguments[1].asFloat(), // volume
                            arguments[2].asFloat() // pitch
                    );
                    return null;
                };

            case "kick":
                return (ProxyExecutable) arguments -> {
                    if (arguments.length < 1) return null;
                    bukkitPlayer.kickPlayer(arguments[0].asString());
                    return null;
                };

            case "teleport":
                return (ProxyExecutable) arguments -> {
                    if (arguments.length < 3) return null;
                    bukkitPlayer.teleport(new Location(
                            bukkitPlayer.getWorld(),
                            arguments[0].asDouble(),
                            arguments[1].asDouble(),
                            arguments[2].asDouble(),
                            arguments.length > 3 ? arguments[3].asFloat() : 0,
                            arguments.length > 4 ? arguments[4].asFloat() : 0
                    ));
                    return null;
                };

            case "giveItem":
                return (ProxyExecutable) arguments -> {
                    if (arguments.length < 2) return null;
                    Material material = Material.matchMaterial(arguments[0].asString());
                    if (material == null) return null;
                    bukkitPlayer.getInventory().addItem(new ItemStack(
                            material,
                            arguments[1].asInt()
                    ));
                    return null;
                };

            default:
                return null;
        }
    }

    @Override
    public Object getMemberKeys() {
        return null;
    }

    @Override
    public boolean hasMember(String key) {
        return Arrays.asList(
                "getName", "getDisplayName", "getUniqueId", "getAddress", "getWorld",
                "getLocation", "getHealth", "getLevel", "getExp", "isOp", "isOnline",
                "hasPermission", "sendMessage", "sendTitle", "playSound", "kick",
                "teleport", "giveItem"
        ).contains(key);
    }

    @Override
    public void putMember(String key, Value value) {

    }
}
