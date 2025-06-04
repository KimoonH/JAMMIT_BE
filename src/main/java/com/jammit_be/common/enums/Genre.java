package com.jammit_be.common.enums;

public enum Genre {
    ROCK("락"),
    METAL("메탈"),
    POP("팝"),
    BALLAD("발라드"),
    INDIE("인디"),
    ALTERNATIVE("얼터너터브"),
    JAZZ("재즈"),
    PUNK("펑크"),
    ACOUSTIC("어쿠스틱"),
    FOLK("포크"),
    RNB("R&B");

    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
