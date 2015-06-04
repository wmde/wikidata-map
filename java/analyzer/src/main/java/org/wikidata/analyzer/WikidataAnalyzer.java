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
     * Main method. Processes the whole dump using this processor and writes the results to disk.
     * This should be executed when in the root of the git repo to ensure the correct local dump dir is used.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        System.out.println("******************************************");
        System.out.println("*** Wikidata Toolkit: WikidataAnalyzer ***");
        System.out.println("******************************************");
        if (Runtime.getRuntime().maxMemory() / 1024 / 1024 <= 1900) {
            System.out.println("WARNING: You may need to increase your memory limit!");
        }
        long startTime = System.currentTimeMillis();
        String dataDirectory = System.getProperty("user.dir") + File.separator + "data" + File.separator;

        DumpProcessingController controller = new DumpProcessingController("wikidatawiki");
        controller.setOfflineMode(false);

        // Create Json output objects
        JSONObject mapGeoData = new JSONObject();
        JSONObject mapGraphData = new JSONObject();

        // Fetch and process dump
        controller.registerEntityDocumentProcessor(new CounterProcessor(), null, true);
        controller.registerEntityDocumentProcessor(new MapProcessor(mapGeoData, mapGraphData), null, true);
        DumpFetcher fetcher = new DumpFetcher();
        System.out.println("Fetching dump");
        MwDumpFile dump = fetcher.getMostRecentDump();
        System.out.println("Processing dump");
        controller.processDump(dump);
        System.out.println("Processed!");
        System.out.println("Memory Usage (MB): " + Runtime.getRuntime().totalMemory() / 1024 / 1024);

        // Create all output files
        System.out.println("Writing map wdlabel.json");
        BufferedWriter mapLabelWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File( dataDirectory + "wdlabel.json"))));
        mapGeoData.writeJSONString(mapLabelWriter);
        mapLabelWriter.close();
        System.out.println("Writing map graph.json");
        BufferedWriter mapGraphWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File( dataDirectory + "graph.json"))));
        mapGraphData.writeJSONString(mapGraphWriter);
        mapGraphWriter.close();

        // Finish up
        System.out.println("All Done!");
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Execution time: " + elapsedSeconds / 60 + ":" + elapsedSeconds % 60);
    }
}
