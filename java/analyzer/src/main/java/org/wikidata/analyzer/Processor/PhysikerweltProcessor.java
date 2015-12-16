package main.java.org.wikidata.analyzer.Processor;

import org.wikidata.wdtk.datamodel.interfaces.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class PhysikerweltProcessor implements EntityDocumentProcessor {

    private Writer writer;

    public PhysikerweltProcessor(Writer writer) throws IOException {
        this.writer = writer;
        this.writer.write("ItemId \"enwiki title\" \"dewiki title\" \"ruwiki title\"\n");
    }

    @Override
    public void processItemDocument(ItemDocument item) {
        Map<String, SiteLink> sitelinks = item.getSiteLinks();

        String outputLine = item.getItemId().toString() + " ";

        if( sitelinks.containsKey( "enwiki" ) ) {
            outputLine = outputLine + "\"" + sitelinks.get( "enwiki" ) + "\" ";
        } else {
            outputLine = outputLine + "\"\" ";
        }

        if( sitelinks.containsKey( "dewiki" ) ) {
            outputLine = outputLine + "\"" + sitelinks.get( "dewiki" ) + "\" ";
        } else {
            outputLine = outputLine + "\"\" ";
        }

        if( sitelinks.containsKey( "ruwiki" ) ) {
            outputLine = outputLine + "\"" + sitelinks.get( "ruwiki" ) + "\" ";
        } else {
            outputLine = outputLine + "\"\" ";
        }

        try {
            this.writer.write(outputLine + "\n");
        } catch (IOException e) {
            System.out.println("Failed to write to writer");
            e.printStackTrace();
        }

    }

    @Override
    public void processPropertyDocument(PropertyDocument property) {
    }

}