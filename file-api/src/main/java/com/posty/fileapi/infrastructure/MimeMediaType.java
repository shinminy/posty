package com.posty.fileapi.infrastructure;

import com.posty.fileapi.dto.MediaType;

public enum MimeMediaType {
    IMAGE("image/"),
    VIDEO("video/"),
    AUDIO("audio/");

    private final String mimePrefix;

    MimeMediaType(String mimePrefix) {
        this.mimePrefix = mimePrefix;
    }

    public static MimeMediaType from(MediaType mediaType) {
        return MimeMediaType.valueOf(mediaType.name());
    }

    public boolean matches(String mimeType) {
        return mimeType != null && mimeType.startsWith(mimePrefix);
    }
}
