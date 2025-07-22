package com.posty.common.domain.post;

public enum MediaType {
    IMAGE("image/"),
    VIDEO("video/"),
    AUDIO("audio/");

    private String mimePrefix;

    MediaType(String mimePrefix) {
        this.mimePrefix = mimePrefix;
    }

    public boolean isValid(String mimeType) {
        return mimeType != null && mimeType.startsWith(this.mimePrefix);
    }
}
