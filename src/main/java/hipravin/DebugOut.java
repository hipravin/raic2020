package hipravin;

import model.ServerMessage;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class DebugOut {
    public static final boolean enabled = "true".equals(System.getProperty("LOCAL"));
    public static Path gameLog = Path.of("../game_log");
    static {
        //clean debug folder
//        if(enabled) {
//           if(Files.isDirectory(gameLog)) {
//               try(Stream<Path> files = Files.walk(gameLog)) {
//                   files.map(Path::toFile).forEach(File::delete);
//               } catch (IOException e) {
//                   e.printStackTrace();
//               };
//           }
//        }
    }

    public static void println(String line) {
        if(enabled) {
            System.out.println(line);
        }
    }

    public static void writeDebugBin(int tick, ServerMessage serverMessage) {
        if(enabled) {
            Path tickNFile = gameLog.resolve("tick" + tick + ".bin");

            deleteIfExists(tickNFile);

            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tickNFile.toFile()))) {
                serverMessage.writeTo(os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void deleteIfExists(Path tickNFile) {
        if(Files.isRegularFile(tickNFile)) {
            try {
                Files.delete(tickNFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
