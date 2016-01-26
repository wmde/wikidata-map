package main.java.org.wikidata.analyzer.Processor;

import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.*;

/**
 * MonolingualTextProcessor for wikidata-analysis
 *
 * Counts the languages used in monolingual text value snaks
 *
 * @author Addshore
 */
public class MonolingualTextProcessor implements EntityDocumentProcessor {

    private Map<String, Long> counters;

    public MonolingualTextProcessor(Map<String, Long> counters) {
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

    @Override
    public void processItemDocument(ItemDocument item) {
        for (Iterator<Statement> statements = item.getAllStatements(); statements.hasNext(); ) {
            this.processStatement( statements.next() );
        }
    }

    @Override
    public void processPropertyDocument(PropertyDocument property) {
        for (Iterator<Statement> statements = property.getAllStatements(); statements.hasNext(); ) {
            this.processStatement( statements.next() );
        }
    }

    private void processStatement( Statement statement ) {
        this.processSnak(statement.getClaim().getMainSnak());
        for (Iterator<Snak> qualifierSnaks = statement.getClaim().getAllQualifiers(); qualifierSnaks.hasNext(); ) {
            Snak qualifierSnak = qualifierSnaks.next();
            this.processSnak(qualifierSnak);
        }
        for (Reference reference : statement.getReferences()) {
            for (Iterator<Snak> referenceSnaks = reference.getAllSnaks(); referenceSnaks.hasNext(); ) {
                Snak referenceSnak = referenceSnaks.next();
                this.processSnak(referenceSnak);
            }
        }
    }

    private void processSnak( Snak snak ) {
        if (snak instanceof ValueSnak) {
            Value value = ((ValueSnak) snak).getValue();
            if (value instanceof MonolingualTextValue) {
                MonolingualTextValue textValue = (MonolingualTextValue) value;
                this.increment( textValue.getLanguageCode() );
            }
        }
    }

}