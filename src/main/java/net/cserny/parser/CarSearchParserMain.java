package net.cserny.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class CarSearchParserMain {

    private static final Path LOGS_PATH = Paths.get("/home/leonardo/Documents/logs/");
    private static final Path OUT_PATH = LOGS_PATH.resolve("out");
    private static final Path COMBINED_FILE = OUT_PATH.resolve("combined.log");
    private static final Path CSV_FILE = OUT_PATH.resolve("report.csv");

    private CarSearchParser parser;
    private CarSearchCsvCreator csvCreator;

    private Map<SearchType, List<Pattern>> searchPatterns = new HashMap<>();
    private Pattern logFilePattern = Pattern.compile(".*\\[(?<date>.*)].*(?<method>GET|POST) (?<uri>/.*) HTTP/1\\.1\" (?<status>[0-9]{3}).*");

    public CarSearchParserMain() {
        initPatterns();

        parser = new CarSearchParser(COMBINED_FILE, LOGS_PATH, logFilePattern, searchPatterns);
        csvCreator = new CarSearchCsvCreator(COMBINED_FILE, CSV_FILE, logFilePattern, searchPatterns);
    }

    private void initPatterns() {
        List<Pattern> rimPatterns = new ArrayList<>();
        rimPatterns.add(Pattern.compile("^/automarken-felgen\\.html/?$"));
        rimPatterns.add(Pattern.compile("^/felgen/(?<carBrand>.*)/(?<carBrand2>.*)-(?<carModel>.*)-felgen\\.html"));
        rimPatterns.add(Pattern.compile("^/felgen/(?!.*-zoll)(?<carBrand>.*)-felgen\\.html"));
        rimPatterns.add(Pattern.compile("^/rims/rim-selector\\?(?:(?=.*type=(?<rimType>[^&]*)))?.*$"));
        searchPatterns.put(SearchType.RIM, rimPatterns);

        List<Pattern> wheelPatterns = new ArrayList<>();
        wheelPatterns.add(Pattern.compile("^/raeder/(?<carBrand>.*)/(?<carBrand2>.*)-komplettraeder\\.html"));
        wheelPatterns.add(Pattern.compile("^/raeder/(?!.*-zoll)(?<carBrand>.*)-komplettraeder\\.html"));
        wheelPatterns.add(Pattern.compile("^/cw/rim-selector\\?(?:(?=.*type=(?<rimType>[^&]*)))?(?:(?=.*season=(?<rimSeason>[^&]*)))?.*$"));
        searchPatterns.put(SearchType.WHEEL, wheelPatterns);

        List<Pattern> advicePatterns = new ArrayList<>();
        advicePatterns.add(Pattern.compile("^/Advice/(?<carBrand>.*)/(?<carBrand2>.*)-(?<carModel>.*)-Reifen\\.html"));
        advicePatterns.add(Pattern.compile("^/Advice/(?!.*-zoll)(?<carBrand>.*)-Reifen\\.html"));
        advicePatterns.add(Pattern.compile("^/autoreifen/saison/(sommerreifen|ganzjahresreifen|nordic|spikes|winterreifen|bespikte_reifen|bespikte reifen|nordic compound)/(?<carModel>.*)/(?<carModel2>.*)"));
        advicePatterns.add(Pattern.compile("^/autoreifen/saison/(sommerreifen|ganzjahresreifen|nordic|spikes|winterreifen|bespikte_reifen|bespikte reifen|nordic compound)/(?<carModel>.*)"));
        searchPatterns.put(SearchType.ADVICE, advicePatterns);

        List<Pattern> oilPatterns = new ArrayList<>();
        oilPatterns.add(Pattern.compile("^/oel/pkw/(?<carBrand>.*)"));
        oilPatterns.add(Pattern.compile("^/oel/pkw/?$"));
        oilPatterns.add(Pattern.compile("^/oils/search\\?(?:(?=.*vehicleId=(?<vehicleId>[^&]*)))?.*$"));
        searchPatterns.put(SearchType.OIL, oilPatterns);

        List<Pattern> snowChainPatterns = Collections.singletonList(
                Pattern.compile("^/search-snow-chain\\?(?:(?=.*vehicleTypes=(?<vehicleType>[^&]*)))?(?:(?=.*vehicleManufacturer=(?<carBrand>[^&]*)))?(?:(?=.*vehicleEngineType=(?<carEngine>[^&]*)))?(?:(?=.*vehicleModel=(?<carModel>[^&]*)))?(?:(?=.*vehicleId=(?<vehicleId>[^&]*)))?(?:(?=.*vehicleSpecification=(?<vehicleSpec>[^&]*)))?.*searchByCar=true.*$")
        );
        searchPatterns.put(SearchType.SNOWCHAIN, snowChainPatterns);

        List<Pattern> tyrePatterns = Collections.singletonList(
                Pattern.compile("^/search\\?(?:(?=.*vehicleTypes=(?<vehicleType>[^&]*)))?(?:(?=.*vehicleManufacturer=(?<carBrand>[^&]*)))?(?:(?=.*vehicleEngineType=(?<carEngine>[^&]*)))?(?:(?=.*vehicleModel=(?<carModel>[^&]*)))?(?:(?=.*vehicleId=(?<vehicleId>[^&]*)))?(?:(?=.*vehicleSpecification=(?<vehicleSpec>[^&]*)))?.*searchByCar=true.*$")
        );
        searchPatterns.put(SearchType.TYRE, tyrePatterns);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            SystemExiter.exitWithMessage("Please provide command as arg: <PARSE> or <CREATE_CSV>");
        }

        CarSearchParserMain logParser = new CarSearchParserMain();
        switch (Command.valueOf(args[0].toUpperCase())) {
            case PARSE:
                logParser.parse();
                break;
            case CREATE_CSV:
                logParser.createCsv();
                break;
            default:
                SystemExiter.exitWithMessage("Error: <COMMAND> unknown");
                break;
        }
    }

    private void parse() throws IOException {
        parser.parse();
    }

    private void createCsv() throws IOException {
        csvCreator.createCsv();
    }
}
