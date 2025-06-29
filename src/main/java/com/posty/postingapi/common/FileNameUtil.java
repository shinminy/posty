package com.posty.postingapi.common;

import java.net.URL;

public class FileNameUtil {

    /**
     * URL에서 파일명 확장자 추출
     * @param fileUrl 원본 파일 URL
     * @return 확장자 (예: ".jpg"), 확장자 없으면 빈 문자열 반환
     */
    public static String extractExtension(URL fileUrl) {
        String path = fileUrl.getPath();
        int lastDotIndex = path.lastIndexOf('.');
        int lastSlashIndex = path.lastIndexOf('/');
        return lastDotIndex > lastSlashIndex && lastDotIndex != -1 ? path.substring(lastDotIndex) : "";
    }
}
