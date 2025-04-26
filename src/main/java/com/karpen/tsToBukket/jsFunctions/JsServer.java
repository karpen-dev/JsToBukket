package com.karpen.tsToBukket.jsFunctions;

import com.karpen.tsToBukket.TsToBukket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.Arrays;

public class JsServer implements ProxyObject {
    private final TsToBukket plugin;

    public JsServer(TsToBukket plugin) {
        this.plugin = plugin;
    }

    @Override
    public Object getMember(String key) {
        switch (key) {
            case "getName":
                return (ProxyExecutable) arguments -> Bukkit.getServer().getName();
            case "getVersion":
                return (ProxyExecutable) arguments -> Bukkit.getServer().getVersion();
            case "getOnlinePlayers":
                return (ProxyExecutable) arguments -> {
                    Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
                    return ProxyArray.fromArray((Object[]) Arrays.stream(players)
                            .map(p -> new JsPlayer(plugin, p))
                            .toArray());
                };
            case "broadcast":
                return (ProxyExecutable) arguments -> {
                    Bukkit.broadcastMessage(arguments[0].asString());
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
        return Arrays.asList("getName", "getVersion", "getOnlinePlayers", "broadcast").contains(key);
    }

    @Override
    public void putMember(String key, Value value) {

    }
}