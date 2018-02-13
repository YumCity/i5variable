package com.i5mc.variable.level;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "i5level_up")
@EqualsAndHashCode(of = "id")
public class LevelUp {

    @Id
    private int id;
    private int xp;
}
