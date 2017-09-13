package com.i5mc.variable;

import com.avaje.ebean.EbeanServer;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created by on 2017/9/12.
 */
public enum L2Pool {

    INST;

    final Map<UUID, CompletableFuture<Stat>> mapping = new HashMap<>();
    private EbeanServer db;

    public void init(EbeanServer db) {
        this.db = db;
    }

    public CompletableFuture<Stat> load(Player p) {
        return INST.mapping.computeIfAbsent(p.getUniqueId(), id -> CompletableFuture.supplyAsync(() -> {
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

    public CompletableFuture<Void> save(Stat any) {
        any.setData(any.object.toJSONString());
        return CompletableFuture.runAsync(() -> db.save(any));
    }

    public void quit(Player p) {
        INST.mapping.remove(p.getUniqueId());
    }

}
