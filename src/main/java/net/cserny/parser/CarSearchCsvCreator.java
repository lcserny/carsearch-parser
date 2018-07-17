package net.cserny.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CarSearchCsvCreator {

    private final Path combinedFile;
    private final Path csvFile;
    private Pattern logFilePattern;
    private Map<SearchType, List<Pattern>> searchPatterns;

    private String delimiter = ",";
    private String enclosed = "\"";

    public CarSearchCsvCreator(Path combinedFile, Path csvFile, Pattern logFilePattern, Map<SearchType, List<Pattern>> searchPatterns) {
        this.combinedFile = combinedFile;
        this.csvFile = csvFile;
        this.logFilePattern = logFilePattern;
        this.searchPatterns = searchPatterns;
    }

    public void create() throws IOException {
        if (!Files.exists(combinedFile)) {
            SystemExiter.exitWithMessage("Combined file doesn't exist, please parse first, aborting");
        }

        if (Files.exists(csvFile)) {
            SystemExiter.exitWithMessage("CSV already created, aborting");
        }

        Files.createFile(csvFile);
        Files.write(csvFile, getHeaderLine().getBytes());

        Files.lines(combinedFile).forEach(line -> {
            try {
                writeCsvLine(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String getHeaderLine() {
        List<String> columnsHeaders = Arrays.asList(addCsvField("date"), addCsvField("status"), addCsvField("uri"),
                addCsvField("searchType"), addCsvField("carBrand"), addCsvField("carModel"), addCsvField("carEngine"),
                addCsvField("vehicleType"), addCsvField("vehicleId"), addCsvField("vehicleSpec"),
                addCsvField("rimType"), addCsvField("rimSeason"));
        String headerLine = String.join(delimiter, columnsHeaders);
        return headerLine + "\n";
    }

    private String addCsvField(String field) {
        return enclosed + field + enclosed;
    }

    private void writeCsvLine(String line) throws IOException {
        Matcher logLineMatcher = logFilePattern.matcher(line);
        if (logLineMatcher.find()) {
            String date = logLineMatcher.group("date");
            String uri = logLineMatcher.group("uri");
            String status = logLineMatcher.group("status");

            boolean lineDone = false;
            for (Map.Entry<SearchType, List<Pattern>> entry : searchPatterns.entrySet()) {
                for (Pattern pattern : entry.getValue()) {
                    Matcher uriMatcher = pattern.matcher(uri);
                    if (uriMatcher.find()) {
                        if (stopOnCertainParams(uriMatcher)) {
                            lineDone = true;
                            break;
                        }

                        StringBuilder csvLineBuilder = new StringBuilder();
                        csvLineBuilder.append(addCsvField(date));
                        csvLineBuilder.append(delimiter).append(addCsvField(status));
                        csvLineBuilder.append(delimiter).append(addCsvField(uri));
                        csvLineBuilder.append(delimiter).append(addCsvField(entry.getKey().toString()));
                        appendToLine(csvLineBuilder, uriMatcher, "carBrand");
                        appendToLine(csvLineBuilder, uriMatcher, "carModel");
                        appendToLine(csvLineBuilder, uriMatcher, "carEngine");
                        appendToLine(csvLineBuilder, uriMatcher, "vehicleType");
                        appendToLine(csvLineBuilder, uriMatcher, "vehicleId");
                        appendToLine(csvLineBuilder, uriMatcher, "vehicleSpec");
                        appendToLine(csvLineBuilder, uriMatcher, "rimType");
                        appendToLine(csvLineBuilder, uriMatcher, "rimSeason");
                        csvLineBuilder.append("\n");

                        String csvRow = csvLineBuilder.toString();
                        System.out.print(csvRow);

                        Files.write(csvFile, csvRow.getBytes(), StandardOpenOption.APPEND);

                        lineDone = true;
                        break;
                    }
                }

                if (lineDone) {
                    break;
                }
            }
        }
    }

    private void appendToLine(StringBuilder builder, Matcher matcher, String key) {
        builder.append(delimiter);
        try {
            String field = matcher.group(key);
            builder.append(addCsvField(field.toLowerCase()));
        } catch (IllegalArgumentException | NullPointerException ignored) {}
    }

    private boolean stopOnCertainParams(Matcher uriMatcher) {
        boolean stop = false;
        try {
            String carBrand = uriMatcher.group("carBrand");
            String carModel = uriMatcher.group("carModel");
            if (carBrand.isEmpty() || carModel.isEmpty()) {
                stop = true;
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            stop = true;
        }
        return stop;
    }
}
