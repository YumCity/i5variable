package com.i5mc.variable;

import com.avaje.ebean.EbeanServer;
import com.google.common.collect.ImmutableMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.i5mc.variable.level.I5Level;
import com.i5mc.variable.level.LevelExec;
import com.i5mc.variable.level.LevelUp;
import com.mengcraft.simpleorm.EbeanManager;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by on 2017/9/1.
 */
public class Main extends JavaPlugin implements Listener {

    private EbeanServer dataSource;
    private VarExec exec;
    private LevelExec level;
    @Getter
    private static Messenger messenger;
    private static ExecutorService pool;
    @Getter
    private static Global global;

    @Override
    @SneakyThrows
    public void onEnable() {
        saveDefaultConfig();

        val d = EbeanManager.DEFAULT.getHandler(this);
        if (d.isNotInitialized()) {
            d.define(Variable.class);
            d.define(Stat.class);
            d.define(I5Level.class);
            d.define(LevelUp.class);
            d.initialize();
        }
        d.install();
        dataSource = d.getServer();

        pool = Executors.newSingleThreadExecutor();

        messenger = new Messenger(this);

        L2Pool.INSTANCE.init(dataSource);

        new StatExec(this).hook();
        new VarLocalExec(this).hook();

        exec = new VarExec(this);
        exec.hook();

        level = new LevelExec(this);
        level.hook();

        {
            val load = new Yaml().load(new FileInputStream(new File(getDataFolder(), "config.yml")));
            val json = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            global = json.fromJson(json.toJsonTree(load), Global.class);
        }

        PluginHelper.addExecutor(this, new StatCommand());
        PluginHelper.addExecutor(this, "i5level", level);

        Bukkit.getPluginManager().registerEvents(this, this);

        int update = getConfig().getInt("update");
        Bukkit.getScheduler().runTaskTimer(this, () -> runAsync(this::update), 1, update);
    }

    @SneakyThrows
    public void onDisable() {
        if (!(pool == null)) {
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    public void update() {
        val l = dataSource.find(Variable.class).findList();
        ImmutableMap.Builder<String, Variable> b = ImmutableMap.builder();
        for (val variable : l) {
            b.put(variable.getName(), variable);
        }
        val out = b.build();
        getServer().getScheduler().runTask(this, () -> exec.setMapping(out));
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        L2Pool.INSTANCE.quit(event.getPlayer());
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        if (global.getLevel().isInjectXpBar()) {
            runAsync(() -> {
                I5Level level = L2Pool.INSTANCE.level(event.getPlayer());
                getServer().getScheduler().runTask(this, () -> updateXpBar(event.getPlayer(), level));
            });
        }
    }

    public static void runAsync(Runnable runnable) {
        pool.execute(runnable);
    }

    public static void updateXpBar(Player p, I5Level level) {
        p.setLevel(level.getLevel());
        int i = L2Pool.INSTANCE.nextLevel(level);
        p.setExp(i > level.getXp() ? BigDecimal.valueOf(level.getXp()).divide(BigDecimal.valueOf(i), 1, RoundingMode.HALF_UP).floatValue() : 1);
    }

}
