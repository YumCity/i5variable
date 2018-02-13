package com.i5mc.variable.level;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "i5level")
@Data
@EqualsAndHashCode(of = "id")
public class Level {

    @Id
    private UUID id;
    private String name;
    private int level = 1;
    private int xp;
    private int xpTotal;
}
