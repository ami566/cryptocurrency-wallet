package bg.sofia.uni.fmi.mjt.cryptowallet.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorLogger {
    private static final String ERRORS_FILE = "resources/errorsLog.txt";

    public static void logException(Exception e) {
        try (var fileWriter = new FileWriter(ERRORS_FILE)) {
            Logger logger = new Logger(fileWriter);
            logger.logMessage((e.getMessage() + System.lineSeparator() + getStacktraceOfException(e)) + System.lineSeparator());
        } catch (IOException exception) {
            System.out.println("Failed to log exception");
        }
    }

    private static String getStacktraceOfException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return sw.toString();
    }
}
