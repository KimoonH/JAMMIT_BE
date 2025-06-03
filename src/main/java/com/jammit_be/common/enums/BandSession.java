package com.jammit_be.common.enums;

public enum BandSession {
    VOCAL("보컬"),
    ELECTRIC_GUITAR("일렉 기타"),
    DRUM("드럼"),
    ACOUSTIC_GUITAR("통기타"),
    BASS("베이스"),
    STRING_INSTRUMENT("현악기"),
    PERCUSSION("타악기"),
    KEYBOARD("건반");

    private final String displayName;

    BandSession(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
