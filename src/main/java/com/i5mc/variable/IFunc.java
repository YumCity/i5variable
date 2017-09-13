package com.i5mc.variable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;

/**
 * Created by on 2017/9/12.
 */
public interface IFunc<T> {

    T apply(CommandSender p, Iterator<String> input);
}
