package main.java.org.wikidata.analyzer.Processor;

import com.google.common.collect.Iterators;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.List;
import java.util.Map;

/**
 * @author Addshore
 */
public class CounterProcessor {

    private Map<String, Long> counters;

    public CounterProcessor(Map<String, Long> counters){
        this.counters = counters;
    }

    private void increment(String counter) {
        this.increment(counter, 1);
    }

    private void increment(String counter, int quantity) {
        this.initiateCounterIfNotReady(counter);
        this.counters.put(counter, this.counters.get(counter) + (long) quantity);
    }

    private void initiateCounterIfNotReady(String counter) {
        if (!this.counters.containsKey(counter)) {
            this.counters.put(counter, (long) 0);
        }
    }

    public void processItemDocument(ItemDocument document) {
        if (document != null) {
            String type = "item";
            this.increment(type);
            this.processTermedDocument(document, type);
            this.processStatementDocument(document, type);
            this.increment("item.sitelink", document.getSiteLinks().size());
        }
    }

    public void processPropertyDocument(PropertyDocument document) {
        if (document != null) {
            String type = "property";
            this.increment(type);
            this.processTermedDocument(document, type);
            this.processStatementDocument(document, type);
        }
    }

    private void processTermedDocument(TermedDocument document, String type) {
        this.increment(type + ".label", document.getLabels().size());
        this.increment(type + ".description", document.getDescriptions().size());
        this.increment(type + ".aliasgroup", document.getAliases().size());
        for (Map.Entry<String, List<MonolingualTextValue>> aliasGroup : document.getAliases().entrySet()) {
            this.increment(type + ".alias", aliasGroup.getValue().size());
        }
    }

    private void processStatementDocument(StatementDocument document, String type) {
        this.increment(type + ".statement", Iterators.size(document.getAllStatements()));
    }
}
