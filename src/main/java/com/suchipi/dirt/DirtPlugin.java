package com.suchipi.dirt;

import jdk.nashorn.api.scripting.JSObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.script.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;


@SuppressWarnings("unused")
public final class DirtPlugin extends JavaPlugin {
    ScriptEngine engine;

    @Override
    public void onEnable() {
        System.setProperty("nashorn.args", "--language=es6 --global-per-engine");
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.put("plugin", this);

        try {
            engine.eval(
                "var global = this;\n" +
                "global.console = {};\n" +
                "console.debug = print;\n" +
                "console.log = print;\n" +
                "console.warn = print;\n" +
                "console.error = print;\n" +
                "global.mitt = (function n(n){return n=n||Object.create(null),{on:function(c,e){(n[c]||(n[c]=[])).push(e)},off:function(c,e){n[c]&&n[c].splice(n[c].indexOf(e)>>>0,1)},emit:function(c,e){(n[c]||[]).slice().map(function(n){n(e)}),(n[\"*\"]||[]).slice().map(function(n){n(c,e)})}}})"
            );
            engine.eval(
                "global.events = mitt(); events.normal = events; events.monitor = mitt(); events.highest = mitt(); events.high = mitt(); events.low = mitt(); events.lowest = mitt();"
            );
        } catch (Exception error) {
            getLogger().log(Level.SEVERE, "Failed to initialize Nashorn engine: " + error.getMessage());
        }

        bindJSEvents(EventPriority.NORMAL, "normal");
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

    private void bindJSEvents(EventPriority priority, String priorityString) {
        RegisteredListener registeredListener = new RegisteredListener(new Listener() {}, (listener, event) -> {
            List<Class> ignoredEvents = Arrays.asList(new Class[] {
                    BlockFadeEvent.class,
                    EntityAirChangeEvent.class,
                    PlayerMoveEvent.class,
                    PlayerAnimationEvent.class,
                    BlockPhysicsEvent.class,
                    VehicleUpdateEvent.class,
                    ChunkLoadEvent.class,
            });
            if (ignoredEvents.contains(event.getClass())) return;

            Bindings bindings = engine.createBindings();
            bindings.put("eventName", event.getClass().getName());
            bindings.put("event", event);
            try {
                engine.eval("events." + priorityString + ".emit(eventName, event)", bindings);
            } catch(Exception error) {
                Bukkit.getServer().broadcastMessage("Error in JS Event Handler: " + error.getMessage());
            }
        }, priority, this, true);

        for (HandlerList handler : HandlerList.getHandlerLists()) {
            handler.register(registeredListener);
        }
    }
}
