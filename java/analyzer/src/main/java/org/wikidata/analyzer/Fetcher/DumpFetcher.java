package main.java.org.wikidata.analyzer.Fetcher;

import org.wikidata.wdtk.dumpfiles.MwDumpFile;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;
import org.wikidata.wdtk.dumpfiles.wmf.JsonOnlineDumpFile;
import org.wikidata.wdtk.util.DirectoryManager;
import org.wikidata.wdtk.util.DirectoryManagerImpl;
import org.wikidata.wdtk.util.WebResourceFetcher;
import org.wikidata.wdtk.util.WebResourceFetcherImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Addshore
 */
public class DumpFetcher {

    protected File dataDirectory;

    public DumpFetcher(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * Look for the most recent dump date online and try to retrieve as dump object with fallback:
     * 1 - Look for local dump copies (in a collection of locations)
     * 2 - Look online & download dumps
     *
     * @return MwDumpFile
     * @throws IOException
     */
    public MwDumpFile getMostRecentDump() throws IOException {
        DumpDateFetcher dateFetcher = new DumpDateFetcher();
        String latestDumpDate = dateFetcher.getLatestOnlineDumpDate();
        System.out.println("Latest dump date stamp is " + latestDumpDate);

        // Look for the dump in a list of possible local locations
        List<String> locationList = new ArrayList<>();
        //Local data dir location
        locationList.add(this.dataDirectory + "/dumpfiles/json-" + latestDumpDate + "/" + latestDumpDate + "-all.json.gz");
        //Labs dump location
        locationList.add("/public/dumps/public/wikidatawiki/entities/" + latestDumpDate + "/wikidata-" + latestDumpDate + "-all.json.gz");
        //Stat1002 dump location
        locationList.add("/mnt/data/xmldatadumps/public/wikidatawiki/entities/" + latestDumpDate + "/wikidata-" + latestDumpDate + "-all.json.gz");
        for (String dumpLocation: locationList) {
            if (Files.exists(Paths.get(dumpLocation)) && Files.isReadable(Paths.get(dumpLocation))) {
                MwLocalDumpFile localDumpFile = new MwLocalDumpFile( dumpLocation );
                if( localDumpFile.isAvailable() ) {
                    System.out.println("Using dump file from: " + dumpLocation);
                    return localDumpFile;
                }
            }
        }

        // Fallback to downloading the dump ourselves
        DirectoryManager localDirectoryManager = new DirectoryManagerImpl(
                Paths.get(this.dataDirectory.getAbsolutePath() + File.separator + "dumpfiles"),
                false
        );
        WebResourceFetcher fetcher = new WebResourceFetcherImpl();
        JsonOnlineDumpFile onlineDumpFile = new JsonOnlineDumpFile(
                latestDumpDate,
                "wikidatawiki",
                fetcher,
                localDirectoryManager
        );
        if (onlineDumpFile.isAvailable()) {
            System.out.println("Using online dump file");
            return onlineDumpFile;
        }

        throw new IOException("Failed to get most recent dump from any sources");
    }
}
