package main.java.org.wikidata.analyzer;

import main.java.org.wikidata.analyzer.Processor.BadDateProcessor;
import main.java.org.wikidata.analyzer.Processor.CounterProcessor;
import main.java.org.wikidata.analyzer.Fetcher.DumpFetcher;
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

        // Get the data directory
        File dataDir = null;
        try{
            dataDir = new File(args[0]);
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

        if (Runtime.getRuntime().maxMemory() / 1024 / 1024 <= 1500) {
            System.out.println("WARNING: You may need to increase your memory limit!");
        }
        long startTime = System.currentTimeMillis();

        File list1 = new File( dataDir.getAbsolutePath() + File.separator + "date_list1.txt");
        BufferedWriter list1Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list1)));
        File list2 = new File( dataDir.getAbsolutePath() + File.separator + "date_list2.txt");
        BufferedWriter list2Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list2)));

        DumpProcessingController controller = new DumpProcessingController("wikidatawiki");
        controller.setOfflineMode(false);

        // Fetch and process dump
        controller.registerEntityDocumentProcessor(new CounterProcessor(), null, true);
        controller.registerEntityDocumentProcessor(new BadDateProcessor(list1Writer, list2Writer), null, true);
        DumpFetcher fetcher = new DumpFetcher(dataDir);
        System.out.println("Fetching dump");
        MwDumpFile dump = fetcher.getMostRecentDump();
        System.out.println("Processing dump");
        controller.processDump(dump);
        System.out.println("Processed!");
        System.out.println("Memory Usage (MB): " + Runtime.getRuntime().totalMemory() / 1024 / 1024);

        // Close the writers
        list1Writer.close();
        list2Writer.close();

        // Finish up
        System.out.println("All Done!");
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Execution time: " + elapsedSeconds / 60 + ":" + elapsedSeconds % 60);
    }
}
