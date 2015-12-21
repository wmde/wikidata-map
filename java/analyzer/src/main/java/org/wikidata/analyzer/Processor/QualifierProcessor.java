package main.java.org.wikidata.analyzer.Processor;

import com.google.common.collect.Iterators;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Addshore
 * @author Tarrow
 */
public class QualifierProcessor implements EntityDocumentProcessor {

    private Map<String, Long> counters;

    public QualifierProcessor(Map<String, Long> counters) {
        this.counters = counters;
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
    public void processItemDocument(ItemDocument itemDocument) {
        if (itemDocument != null) {
            this.processStatementDocument(itemDocument);
        }
    }

    private void processStatementDocument(StatementDocument statementDocument) {
        for (Iterator<Statement> statementIterator = statementDocument.getAllStatements(); statementIterator.hasNext(); ) {
            Statement statement = statementIterator.next();
            processStatement(statement);
        }
    }

    private void processStatement(Statement statement) {
        this.increment("qualifiers", Iterators.size(statement.getClaim().getAllQualifiers()));
    }

    @Override
    public void processPropertyDocument(PropertyDocument propertyDocument) {

    }
}
