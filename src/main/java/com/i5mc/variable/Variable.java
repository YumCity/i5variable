package com.i5mc.variable;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by on 2017/9/1.
 */
@Entity
@Data
@EqualsAndHashCode(of = "id")
public class Variable {

    @Id
    private int id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String value;
}
