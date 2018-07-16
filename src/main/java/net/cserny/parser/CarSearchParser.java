package net.cserny.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CarSearchParser {

    private final Path combinedFile;
    private final Path logsPath;
    private final Pattern logFilePattern;
    private Map<SearchType, List<Pattern>> searchPatterns;

    public CarSearchParser(Path combinedFile, Path logsPath, Pattern logFilePattern, Map<SearchType, List<Pattern>> searchPatterns) {
        this.combinedFile = combinedFile;
        this.logsPath = logsPath;
        this.logFilePattern = logFilePattern;
        this.searchPatterns = searchPatterns;
    }

    public void parse() throws IOException {
        if (Files.exists(combinedFile)) {
            SystemExiter.exitWithMessage("Combined file already exists, aborting");
        }

        Files.createFile(combinedFile);
        Files.walk(logsPath, 1).filter(Files::isRegularFile).forEach(path -> {
            try {
                parseLogFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void parseLogFile(Path path) throws IOException {
        Files.lines(path).filter(this::filterLogLine).forEach(line -> {
            String fullLine = line + "\n";
            System.out.print(fullLine);
            try {
                Files.write(combinedFile, fullLine.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean filterLogLine(String line) {
        Matcher logLineMatcher = logFilePattern.matcher(line);
        if (logLineMatcher.find()) {
            String uri = logLineMatcher.group("uri");
            for (Map.Entry<SearchType, List<Pattern>> entry : searchPatterns.entrySet()) {
                for (Pattern pattern : entry.getValue()) {
                    Matcher uriMatcher = pattern.matcher(uri);
                    if (uriMatcher.find()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
