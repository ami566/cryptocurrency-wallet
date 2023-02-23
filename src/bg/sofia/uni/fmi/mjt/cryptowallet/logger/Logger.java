package bg.sofia.uni.fmi.mjt.cryptowallet.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class Logger {
    private Writer writer;

    public Logger(Writer w) {
        writer = w;
    }

    public void logMessage(String message) {
        try (var bw = new BufferedWriter((writer))) {
            bw.write(message);
        } catch (IOException e) {
            System.out.println("Logging the message failed");
        }
    }
}
