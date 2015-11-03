package main.java.org.wikidata.analyzer.Processor;

import com.google.common.collect.Iterators;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Addshore
 */
public class CounterProcessor {

    private Map<String, Long> counters;

    public CounterProcessor(Map<String, Long> counters) {
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

    /**
     * Generates:
     * - item
     * - item.sitelink.(total|perentity|persite)
     */
    public void processItemDocument(ItemDocument document) {
        if (document != null) {
            String type = "item";
            this.increment(type + ".total");
            this.processTermedDocument(document, type);
            this.processStatementDocument(document, type);
            this.increment(type + ".sitelink.total", document.getSiteLinks().size());
            this.increment(type + ".sitelink.perentity." + document.getSiteLinks().size());
            for (Map.Entry<String, SiteLink> siteLinkEntry : document.getSiteLinks().entrySet()) {
                this.increment(type + ".sitelink.persite." + siteLinkEntry.getKey());
            }
        }
    }

    /**
     * Generates:
     * - property
     * - property.datatype.*.total
     */
    public void processPropertyDocument(PropertyDocument document) {
        if (document != null) {
            String type = "property";
            this.increment(type + ".total");
            this.processTermedDocument(document, type);
            this.processStatementDocument(document, type);
            this.increment(type + ".datatype." + document.getDatatype() + ".total");
        }
    }

    /**
     * Generates:
     * - *.(label|description|aliasgroup).(total|perentity|perlang)
     * - *.alias.(total|perlang)
     */
    private void processTermedDocument(TermedDocument document, String type) {
        this.increment(type + ".label.total", document.getLabels().size());
        this.increment(type + ".label.perentity." + document.getLabels().size());
        for (Map.Entry<String, MonolingualTextValue> labelEntry : document.getLabels().entrySet()) {
            this.increment(type + ".label.perlang." + labelEntry.getKey());
        }

        this.increment(type + ".description.total", document.getDescriptions().size());
        this.increment(type + ".description.perentity." + document.getDescriptions().size());
        for (Map.Entry<String, MonolingualTextValue> descriptionEntry : document.getDescriptions().entrySet()) {
            this.increment(type + ".description.perlang." + descriptionEntry.getKey());
        }

        this.increment(type + ".aliasgroup.total", document.getAliases().size());
        this.increment(type + ".aliasgroup.perentity." + document.getAliases().size());
        for (Map.Entry<String, List<MonolingualTextValue>> aliasGroup : document.getAliases().entrySet()) {
            this.increment(type + ".aliasgroup.perlang." + aliasGroup.getKey());
            this.increment(type + ".alias.total", aliasGroup.getValue().size());
            this.increment(type + ".alias.perlang." + aliasGroup.getKey(), aliasGroup.getValue().size());
        }
    }

    /**
     * Generates:
     * - *.statement.total
     * - *.statement.rank.*.total
     * - *.statement.reference.total
     * - *.statement.reference.snak.total
     * - *.statement.reference.perreference.*
     * - *.statement.qualifier.total
     */
    private void processStatementDocument(StatementDocument document, String type) {
        this.increment(type + ".statement.total", Iterators.size(document.getAllStatements()));

        for (Iterator<Statement> statementIterator = document.getAllStatements(); statementIterator.hasNext(); ) {
            Statement statement = statementIterator.next();

            this.increment(type + ".statement.rank." + statement.getRank() + ".total");

            this.processSnak(statement.getClaim().getMainSnak(), type + ".statement.mainsnak");

            this.increment(type + ".statement.reference.total", statement.getReferences().size());
            for (Reference reference : statement.getReferences()) {
                this.increment(type + ".statement.reference.snak.total", Iterators.size(reference.getAllSnaks()));
                this.increment(type + ".statement.reference.snak.perreference." + Iterators.size(reference.getAllSnaks()));
                //TODO also by reference type? (How do we decide which snak is the main type?)
                //TODO also count references to Wikipedia? (and per type?)
            }

            this.increment(type + ".statement.qualifier.total", Iterators.size(statement.getClaim().getAllQualifiers()));
            for (Iterator<Snak> qualifiersIterator = statement.getClaim().getAllQualifiers(); qualifiersIterator.hasNext(); ) {
                this.processSnak(qualifiersIterator.next(), type + ".statement.qualifier");
            }

        }
    }

    /**
     * Generates:
     * - *.type.(some|no)value.total
     * - *.type.value.perclass.*.total
     */
    private void processSnak(Snak snak, String path) {
        this.increment(path + ".total");
        if (snak instanceof ValueSnak) {
            this.increment(path + ".type.value.total");
            this.increment(path + ".type.value.perclass." + ((ValueSnak) snak).getValue().getClass() + ".total");
        } else if (snak instanceof SomeValueSnak) {
            this.increment(path + ".type.somevalue.total");
        } else if (snak instanceof NoValueSnak) {
            this.increment(path + ".type.novalue.total");
        }
    }

}
