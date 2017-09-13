package com.i5mc.variable;

import com.avaje.ebean.EbeanServer;
import com.google.common.collect.ImmutableMap;
import com.mengcraft.simpleorm.EbeanManager;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Created by on 2017/9/1.
 */
public class Main extends JavaPlugin implements Listener {

    private EbeanServer pool;
    private VarExec exec;

    @Override
    @SneakyThrows
    public void onEnable() {
        saveDefaultConfig();

        val d = EbeanManager.DEFAULT.getHandler(this);
        if (d.isNotInitialized()) {
            d.define(Variable.class);
            d.define(Stat.class);
            d.initialize();
        }
        d.install();
        pool = d.getServer();

        L2Pool.INST.init(pool);

        new StatExec(this).hook();
        new VarLocalExec(this).hook();

        exec = new VarExec(this);
        exec.hook();

        PluginHelper.addExecutor(this, new StatCommand());

        Bukkit.getPluginManager().registerEvents(this, this);

        int update = getConfig().getInt("update");
        Bukkit.getScheduler().runTaskTimer(this, () -> runAsync(this::update), 1, update);
    }

    public void update() {
        val l = pool.find(Variable.class).findList();
        ImmutableMap.Builder<String, Variable> b = ImmutableMap.builder();
        for (val variable : l) {
            b.put(variable.getName(), variable);
        }
        val out = b.build();
        getServer().getScheduler().runTask(this, () -> exec.setMapping(out));
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        L2Pool.INST.quit(event.getPlayer());
    }

}
