package com.posty.fileapi.service;

import com.posty.fileapi.error.InvalidRangeException;

public record ByteRange(
        long start,
        long end,
        long totalSize,
        boolean partial
) {

    public static ByteRange parse(String rangeHeader, long totalSize) {
        if (rangeHeader == null || rangeHeader.isBlank()) {
            return new ByteRange(0, totalSize - 1, totalSize, false);
        }

        if (!rangeHeader.startsWith("bytes=")) {
            throw new InvalidRangeException("Invalid Range header");
        }

        String[] parts = rangeHeader.substring(6).split("-", 2);
        long start = parts[0].isBlank() ? 0 : Long.parseLong(parts[0]);
        long end = parts[1].isBlank() ? totalSize - 1 : Long.parseLong(parts[1]);

        if (start < 0 || end < start || end >= totalSize) {
            throw new InvalidRangeException("Invalid Range values");
        }

        return new ByteRange(start, end, totalSize, true);
    }

    public long length() {
        return end - start + 1;
    }

    public String toContentRangeHeader() {
        return "bytes %d-%d/%d".formatted(start, end, totalSize);
    }
}
