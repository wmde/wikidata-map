package main.java.org.wikidata.analyzer.Processor;

import org.wikidata.wdtk.datamodel.interfaces.*;

/**
 * @author Addshore
 */
public class CounterProcessor implements EntityDocumentProcessor {

    private int itemCount = 0;

    @Override
    public void processItemDocument(ItemDocument item) {
        this.itemCount++;
        //Output a line ever 250,000 items
        if (this.itemCount % 250000 == 0) {
            System.out.println("Processed " + this.itemCount + " items " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB mem used");
        }
    }

    @Override
    public void processPropertyDocument(PropertyDocument property) {
    }

}