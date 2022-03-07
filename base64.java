import java.io.BufferedOutputStream;
import java.util.Base64;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;

public class base64 {
    static void writeToFile(String fileName, String content) {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        Path path = Paths.get("./" + fileName);

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path, CREATE))) {
            out.write(data, 0, data.length);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    static String decodeBase64(String encoded) {
        return new String(Base64.getDecoder().decode(encoded.getBytes(StandardCharsets.UTF_8)));
    }

    static String encodeBase64(byte[] raw) {
        return new String(Base64.getEncoder().withoutPadding().encode(raw));
    }
    static String encodeBase64(String raw) {
        // return new String(Base64.encodeBase64(raw.getBytes(StandardCharsets.UTF_8)));
        return Base64.getEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}