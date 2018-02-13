package com.i5mc.variable;

import lombok.Data;

@Data
public class Global {

    private Level level;

    @Data
    public class Level {

        private boolean autoLevelUp;
        private boolean injectXpBar;
    }
}
