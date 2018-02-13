package com.i5mc.variable;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Update;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.i5mc.variable.level.I5Level;
import com.i5mc.variable.level.LevelUp;
import lombok.SneakyThrows;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Created by on 2017/9/12.
 */
public enum L2Pool {

    INSTANCE;

    private final Cache<String, Object> pool = CacheBuilder.newBuilder().build();
    private final Object invalid = new Object();
    private EbeanServer db;

    public void init(EbeanServer db) {
        this.db = db;
    }

    public I5Level level(Player p) {
        UUID id = p.getUniqueId();
        return fetch(id + ":level", () -> {
            I5Level level = db.find(I5Level.class, id);
            if (level == null) {
                level = db.createEntityBean(I5Level.class);
                level.setId(id);
                level.setName(p.getName());
            }
            return level;
        });
    }

    @SneakyThrows
    public CompletableFuture<Stat> load(Player p) {
        UUID id = p.getUniqueId();
        return fetch(id + ":stat", () -> CompletableFuture.supplyAsync(() -> {
            var find = db.find(Stat.class, id);
            if (find == null) {
                find = db.createEntityBean(Stat.class);
                find.setId(id);
                find.setName(p.getName());
                find.setObject(new JSONObject());
            } else {
                val data = find.getData();
                if (data == null || data.isEmpty()) {
                    find.setObject(new JSONObject());
                } else {
                    find.setObject(((JSONObject) JSONValue.parse(data)));
                }
            }
            return find;
        }));
    }

    public void save(Stat any) {
        any.setData(any.object.toJSONString());
        Main.runAsync(() -> db.save(any));
    }

    @SneakyThrows
    public <T> T fetch(String key, Supplier<T> supplier) {
        val output = INSTANCE.pool.get(key, () -> {
            T value = supplier.get();
            if (value == null) {
                return invalid;
            }
            return value;
        });
        return output == invalid ? null : (T) output;
    }

    public void quit(Player p) {
        String namespace = p.getUniqueId() + ":";
        pool.asMap().keySet().removeIf(key -> key.startsWith(namespace));
    }

    public int nextLevel(I5Level level) {
        return fetch("level_xp:" + level.getLevel(), () -> {
            LevelUp up = db.find(LevelUp.class, level.getLevel());
            return up == null ? Integer.MAX_VALUE : up.getXp();
        });
    }

    public void save(I5Level level) {// This func run async
        Main.runAsync(() -> {
            Update<I5Level> sql = db.createUpdate(I5Level.class, "update i5level set level = :level, xp = :xp, xp_total = :xp_total where id = :id")
                    .set("id", level.getId())
                    .set("level", level.getLevel())
                    .set("xp", level.getXp())
                    .set("xp_total", level.getXpTotal());

            if (!(sql.execute() == 1)) db.insert(level);
        });
    }
}
