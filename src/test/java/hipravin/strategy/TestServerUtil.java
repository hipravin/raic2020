package hipravin.strategy;

import hipravin.DebugOut;
import model.ServerMessage;

import java.io.*;
import java.nio.file.Path;

public abstract class TestServerUtil {
    private final static Path gameLogsDir = Path.of("src/test/resources/gameLogs");

    public static ServerMessage.GetAction readGet(int round, int sampleNum, int tickNum) {
        return (ServerMessage.GetAction) readFromFile(sampleTickFile(round, sampleNum, tickNum));
    }
    public static Path sampleTickFile(int round, int sampleNum, int tickNum) {
        return gameLogsDir
                .resolve("round" + round)
                .resolve("sample" + sampleNum)
                .resolve("tick" + tickNum + ".bin");
    }

    public static ServerMessage readFromFile(Path tickNFile) {
        try(InputStream is = new BufferedInputStream(new FileInputStream(tickNFile.toFile()))) {
            return ServerMessage.readFrom(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TestServerUtil() {
    }

}
