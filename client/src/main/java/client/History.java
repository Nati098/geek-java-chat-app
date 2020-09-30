package client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class History {
    private static final String filenamePattern = "history/history_%s.txt";
    private static final int HISTORY_LIMIT = 100; // how many strokes we should add in the beginning

    private static PrintWriter printWriter;

    public static void start(String login) {
        try {
            printWriter = new PrintWriter(new FileOutputStream(String.format(filenamePattern, login)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (printWriter != null) {
            printWriter.close();
        }
    }

    public static void writeToFile(String msg) {
        printWriter.write(msg);
    }

    public static String getLastMessages(String login) {
        String filename = String.format(filenamePattern, login);

        if (!Files.exists(Paths.get(filename))) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            List<String> historyLines = Files.readAllLines(Paths.get(filename));
            int startPosition = 0;
            if (historyLines.size() > HISTORY_LIMIT) {
                startPosition = historyLines.size() - HISTORY_LIMIT;
            }
            for (int i = startPosition; i < historyLines.size(); i++) {
                sb.append(historyLines.get(i)).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
