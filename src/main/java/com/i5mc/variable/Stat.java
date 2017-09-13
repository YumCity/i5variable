package com.i5mc.variable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.json.simple.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.UUID;

/**
 * Created by on 2017/9/12.
 */
@Data
@Entity
@EqualsAndHashCode(of = "id")
public class Stat {

    @Id
    private UUID id;
    private String name;

    @Column(columnDefinition = "text")
    private String data;

    @Transient
    JSONObject object;
}
