package com.posty.postingapi.infrastructure.file;

import com.posty.postingapi.config.MediaConfig;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

@Component
public class ClamAVScanner {

    private final String host;
    private final int port;

    public ClamAVScanner(MediaConfig mediaConfig) {
        MediaConfig.ClamAVConfig clamAVConfig = mediaConfig.getClamav();
        host = clamAVConfig.getHost();
        port = clamAVConfig.getPort();
    }

    public boolean scanFile(String filePath) throws Exception {
        try (Socket socket = new Socket(host, port);
             OutputStream out = socket.getOutputStream();
             FileInputStream fis = new FileInputStream(filePath)) {

            out.write("zINSTREAM\0".getBytes());

            byte[] buffer = new byte[2048];
            int read;
            while ((read = fis.read(buffer)) >= 0) {
                byte[] size = {
                        (byte) ((read >> 24) & 0xff),
                        (byte) ((read >> 16) & 0xff),
                        (byte) ((read >> 8) & 0xff),
                        (byte) (read & 0xff)
                };
                out.write(size);
                out.write(buffer, 0, read);
            }

            out.write(new byte[]{0, 0, 0, 0});
            out.flush();

            byte[] response = socket.getInputStream().readAllBytes();
            String result = new String(response);
            return result.contains("OK");
        }
    }
}
