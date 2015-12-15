package main.java.org.wikidata.analyzer;

import main.java.org.wikidata.analyzer.Processor.BadDateProcessor;
import main.java.org.wikidata.analyzer.Processor.NoisyProcessor;
import main.java.org.wikidata.analyzer.Processor.MapProcessor;
import main.java.org.wikidata.analyzer.Fetcher.DumpFetcher;
import main.java.org.wikidata.analyzer.Processor.ReferenceProcessor;
import org.json.simple.JSONObject;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwDumpFile;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Addshore
 */
public class WikidataAnalyzer {

    /**
     * Folder that all output should be stored in
     */
    private File dataDir = null;

    /**
     * The target date string or null for latest
     */
    private String targetDate = null;

    /**
     * A list of processors that need to be run
     */
    private List processors = null;

    /**
     * Main method. Instantiates and runs the analyzer
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) throws IOException {
        WikidataAnalyzer analyzer = new WikidataAnalyzer(args);
        analyzer.run();
    }

    /**
     * @param args Command line arguments
     *             A collection of Processors to run (each as a single argument) eg. BadDate Map
     *             The data directory to use eg. ~/data
     *             The date of the dump to target eg. latest OR 20151230
     *             eg. java -Xmx2g -jar ~/wikidata-analyzer.jar Reference ~/data latest
     */
    public WikidataAnalyzer(String[] args) {
        // Output a pretty banner
        System.out.println("******************************************");
        System.out.println("*** Wikidata Toolkit: WikidataAnalyzer ***");
        System.out.println("******************************************");

        // Get the target date
        try {
            targetDate = args[args.length - 1];
        } catch (ArrayIndexOutOfBoundsException exception) {
            System.out.println("Error: Not enough parameters. You must pass a data dir and a target date!");
            System.exit(1);
        }
        if (targetDate.equals("latest")) {
            targetDate = null;
            System.out.println("Targeting latest dump");
        } else {
            System.out.println("Targeting dump from: " + targetDate);
        }
        args = Arrays.copyOf(args, args.length - 1);

        // Get the data directory
        try {
            dataDir = new File(args[args.length - 1]);
            if (!dataDir.exists()) {
                System.out.println("Error: Data directory specified does not exist.");
                System.exit(1);
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            System.out.println("Error: Not enough parameters. You must pass a data dir and a target date!");
            System.exit(1);
        }
        System.out.println("Using data directory: " + dataDir.getAbsolutePath());
        args = Arrays.copyOf(args, args.length - 1);

        // Get the list of processors
        for (String value : args) {
            try {
                Class.forName("main.java.org.wikidata.analyzer.Processor." + value + "Processor");
            } catch (ClassNotFoundException e) {
                System.out.println("Error: " + value + "Processor not found");
                System.exit(1);
            }
            System.out.println(value + "Processor enabled");
        }
        processors = Arrays.asList(args);

        // Check memory limit
        if (Runtime.getRuntime().maxMemory() / 1024 / 1024 <= 1500) {
            System.out.println("WARNING: You may need to increase your memory limit!");
        }
    }

    /**
     * Processes the whole dump with given processors writing output to disk
     */
    public void run() throws IOException {
        // Start up
        long startTime = System.currentTimeMillis();
        DumpProcessingController controller = new DumpProcessingController("wikidatawiki");
        controller.setOfflineMode(false);

        // Reference
        Map<String, Long> referenceCounters = new HashMap<>();
        if (processors.contains("Reference")) {
            controller.registerEntityDocumentProcessor(new ReferenceProcessor(referenceCounters), null, true);
        }

        // Map
        JSONObject mapGeoData = new JSONObject();
        JSONObject mapGraphData = new JSONObject();
        if (processors.contains("Map")) {
            controller.registerEntityDocumentProcessor(new MapProcessor(mapGeoData, mapGraphData), null, true);
        }

        // BadDate
        BufferedWriter list1Writer = null;
        BufferedWriter list2Writer = null;
        if (processors.contains("BadDate")) {
            File list1 = new File(dataDir.getAbsolutePath() + File.separator + "date_list1.txt");
            list1Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list1)));
            File list2 = new File(dataDir.getAbsolutePath() + File.separator + "date_list2.txt");
            list2Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list2)));
            controller.registerEntityDocumentProcessor(new BadDateProcessor(list1Writer, list2Writer), null, true);
        }

        // Fetch and process dump
        controller.registerEntityDocumentProcessor(new NoisyProcessor(), null, true);
        DumpFetcher fetcher = new DumpFetcher(dataDir);
        System.out.println("Fetching dump");
        MwDumpFile dump;
        if(targetDate == null) {
            dump = fetcher.getMostRecentDump();
        } else {
            dump = fetcher.getDump(targetDate);
        }
        dump.prepareDumpFile();
        System.out.println("Processing dump");
        controller.processDump(dump);
        System.out.println("Processed!");
        System.out.println("Memory Usage (MB): " + Runtime.getRuntime().totalMemory() / 1024 / 1024);

        // BadDate
        if (list1Writer != null) {
            list1Writer.close();
        }
        if (list2Writer != null) {
            list2Writer.close();
        }

        // Reference
        if (processors.contains("Reference")) {
            File referenceJsonFile = new File(dataDir.getAbsolutePath() + File.separator + "reference.json");
            BufferedWriter referenceJsonWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(referenceJsonFile)));
            new JSONObject(referenceCounters).writeJSONString(referenceJsonWriter);
            referenceJsonWriter.close();
        }

        // Map
        if (processors.contains("Map")) {
            System.out.println("Writing map wdlabel.json");
            File mapLabelFile = new File(dataDir.getAbsolutePath() + File.separator + "wdlabel.json");
            BufferedWriter mapLabelWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapLabelFile)));
            mapGeoData.writeJSONString(mapLabelWriter);
            mapLabelWriter.close();
            System.out.println("Writing map graph.json");
            File mapGraphFile = new File(dataDir.getAbsolutePath() + File.separator + "graph.json");
            BufferedWriter mapGraphWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapGraphFile)));
            mapGraphData.writeJSONString(mapGraphWriter);
            mapGraphWriter.close();
        }

        // Finish up
        System.out.println("All Done!");
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Execution time: " + elapsedSeconds / 60 + ":" + elapsedSeconds % 60);
    }

}
