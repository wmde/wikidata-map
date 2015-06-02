package org.wikidata.wdanalysis;

import org.json.simple.JSONObject;
import org.wikidata.wdanalysis.Fetcher.DumpFetcher;
import org.wikidata.wdanalysis.Processor.CounterProcessor;
import org.wikidata.wdanalysis.Processor.MapProcessor;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwDumpFile;

import java.io.*;

/**
 * @author Addshore
 */
public class WikidataAnalyzer {

    /**
     * Main method. Processes the whole dump using this processor and writes the results to disk.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        System.out.println("******************************************");
        System.out.println("*** Wikidata Toolkit: WikidataAnalyzer ***");
        System.out.println("******************************************");
        if (Runtime.getRuntime().maxMemory() / 1024 / 1024 <= 1900) {
            System.out.println("Please increase your Java VM max memory to greater than 2GB");
            System.exit(-1);
        }
        long startTime = System.currentTimeMillis();

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
        System.out.println("Writing map wdlabel.js");
        BufferedWriter mapLabelWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("wdlabel.js"))));
        mapLabelWriter.write("var geodata = ");
        mapGeoData.writeJSONString(mapLabelWriter);
        mapLabelWriter.write(";");
        mapLabelWriter.close();
        System.out.println("Writing map graph.js");
        BufferedWriter mapGraphWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("graph.js"))));
        mapGraphWriter.write("var graph = ");
        mapGraphData.writeJSONString(mapGraphWriter);
        mapGraphWriter.write(";");
        mapGraphWriter.close();

        // Finish up
        System.out.println("All Done!");
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Execution time: " + elapsedSeconds / 60 + ":" + elapsedSeconds % 60);
    }
}
