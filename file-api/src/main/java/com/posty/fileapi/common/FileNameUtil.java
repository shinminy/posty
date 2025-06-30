package com.posty.fileapi.common;

import java.net.URL;

public class FileNameUtil {

    /**
     * URL에서 파일명과 확장자를 추출
     * @param fileUrl 원본 파일 URL
     * @return FileNameParts 객체 (이름, 확장자) 예: file, .jpg
     */
    public static FileNameParts parseFileNameFromUrl(URL fileUrl) {
        String path = fileUrl.getPath();
        int lastSlashIndex = path.lastIndexOf('/');
        String fileName = lastSlashIndex != -1 && lastSlashIndex < path.length() - 1
                ? path.substring(lastSlashIndex + 1)
                : "";

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            String name = fileName.substring(0, lastDotIndex);
            String extension = fileName.substring(lastDotIndex);
            return new FileNameParts(name, extension);
        } else {
            return new FileNameParts(fileName, "");
        }
    }
}
