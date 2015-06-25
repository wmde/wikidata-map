package main.java.org.wikidata.analyzer;

import main.java.org.wikidata.analyzer.Processor.CounterProcessor;
import main.java.org.wikidata.analyzer.Processor.MapProcessor;
import main.java.org.wikidata.analyzer.Fetcher.DumpFetcher;
import org.json.simple.JSONObject;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwDumpFile;

import java.io.*;

/**
 * @author Addshore
 */
public class WikidataAnalyzer {

    /**
     * Folder that all output should be stored in
     */
    private File dataDir = null;

    /**
     * Main method. Instantiates and runs the analyzer
     * @param args Command line arguments
     */
    public static void main(String[] args) throws IOException {
        WikidataAnalyzer analyzer = new WikidataAnalyzer( args );
        analyzer.run();
    }

    /**
     * @param args Command line arguments
     */
    public WikidataAnalyzer( String[] args ) {
        // Output a pretty banner
        System.out.println("******************************************");
        System.out.println("*** Wikidata Toolkit: WikidataAnalyzer ***");
        System.out.println("******************************************");

        // Get the data directory
        try{
            dataDir = new File(args[args.length-1]);
            if (!dataDir.exists()) {
                System.out.println("Error: Data directory specified does not exist.");
                System.exit(1);
            }
        }
        catch( ArrayIndexOutOfBoundsException exception ) {
            System.out.println("Error: You must pass a data directory as a parameter.");
            System.exit(1);
        }
        System.out.println("Using data directory: " + dataDir.getAbsolutePath());

        // Check memory limit
        if (Runtime.getRuntime().maxMemory() / 1024 / 1024 <= 1500) {
            System.out.println("WARNING: You may need to increase your memory limit!");
        }
    }

    /**
     * Processes the whole dump using this processor and writes the results to disk.
     * This should be executed when in the root of the git repo to ensure the correct local dump dir is used.
     */
    public void run() throws IOException {
        long startTime = System.currentTimeMillis();

        DumpProcessingController controller = new DumpProcessingController("wikidatawiki");
        controller.setOfflineMode(false);

        // Create Json output objects
        JSONObject mapGeoData = new JSONObject();
        JSONObject mapGraphData = new JSONObject();

        // Fetch and process dump
        controller.registerEntityDocumentProcessor(new CounterProcessor(), null, true);
        controller.registerEntityDocumentProcessor(new MapProcessor(mapGeoData, mapGraphData), null, true);
        DumpFetcher fetcher = new DumpFetcher(dataDir);
        System.out.println("Fetching dump");
        MwDumpFile dump = fetcher.getMostRecentDump();
        System.out.println("Processing dump");
        controller.processDump(dump);
        System.out.println("Processed!");
        System.out.println("Memory Usage (MB): " + Runtime.getRuntime().totalMemory() / 1024 / 1024);

        // Create all output files
        System.out.println("Writing map wdlabel.json");
        File mapLabelFile = new File( dataDir.getAbsolutePath() + File.separator + "wdlabel.json");
        BufferedWriter mapLabelWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapLabelFile)));
        mapGeoData.writeJSONString(mapLabelWriter);
        mapLabelWriter.close();
        System.out.println("Writing map graph.json");
        File mapGraphFile = new File( dataDir.getAbsolutePath() + File.separator + "graph.json");
        BufferedWriter mapGraphWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapGraphFile)));
        mapGraphData.writeJSONString(mapGraphWriter);
        mapGraphWriter.close();

        // Finish up
        System.out.println("All Done!");
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Execution time: " + elapsedSeconds / 60 + ":" + elapsedSeconds % 60);
    }

}
