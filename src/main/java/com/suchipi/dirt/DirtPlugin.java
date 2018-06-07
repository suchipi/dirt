package com.suchipi.dirt;

import jdk.nashorn.api.scripting.JSObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


@SuppressWarnings("unused")
public final class DirtPlugin extends JavaPlugin {
    ScriptEngine engine;

    @Override
    public void onEnable() {
        System.setProperty("nashorn.args", "--language=es6");
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.put("plugin", this);

        try {
            engine.eval(
                    "var global = this;\n" +
                    "var window = this;\n" +
                    "\n" +
                    "var console = {};\n" +
                    "console.debug = print;\n" +
                    "console.log = print;\n" +
                    "console.warn = print;\n" +
                    "console.error = print;"
            );
        } catch (Exception error) {
            getLogger().log(Level.SEVERE, "Failed to initialize Nashorn engine: " + error.getMessage());
        }
    }

    @Override
    public void onDisable() {
        engine = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String script = String.join(" ", args);
        Bukkit.broadcastMessage(sender.getName() + " running JS: " + script);

        try {
            Bindings bindings = engine.createBindings();
            bindings.put("sender", sender);
            if (sender instanceof Player) {
                bindings.put("me", sender);

                bindings.put("world", ((Player) sender).getWorld());

                Block targetBlock = ((Player) sender).getTargetBlock(null, 1000);
                bindings.put("that", targetBlock);

                Location targetBlockLocation = targetBlock.getLocation();
                bindings.put("there", targetBlockLocation);

                Collection<Entity> entities = ((Player) sender).getWorld().getNearbyEntities(targetBlockLocation, 5, 5, 5);
                Bindings tempBindings = engine.createBindings();
                tempBindings.put("entities", entities);
                JSObject entitiesArray = (JSObject) engine.eval("Java.from(entities)", tempBindings);
                bindings.put("those", entitiesArray);
            }
            JSObject players = (JSObject) engine.eval("({})");
            Bukkit.getServer().getOnlinePlayers().forEach((player) -> {
                players.setMember(player.getName(), player);
            });
            bindings.put("players", players);

            Object result;
                result = engine.eval(script, bindings);

            sender.sendMessage(String.valueOf(result));
            return true;
        } catch (Exception error) {
            sender.sendMessage("Error: " + error.getMessage());
            return true;
        }
    }
}
