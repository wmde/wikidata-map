package test.java.org.wikidata.analyzer.Processor;

import junit.framework.TestCase;
import main.java.org.wikidata.analyzer.Processor.QualifierProcessor;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyIdValueImpl;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Addshore
 * @author Tarrow
 */
public class QualifiersProcessorTest extends TestCase {
    private void assertCounter( Map<String, Long> counters, String counter, int expected ) {
        assertTrue( "Assert counter name exists '" + counter + "'", counters.containsKey( counter ) );
        assertEquals( "Assert counter '" + counter + "'value correct", (long)expected, (long)counters.get( counter ) );
    }

    public void testProcessItemDocument() throws Exception {
        Map<String, Long> counters = new HashMap<>();
        QualifierProcessor processor = new QualifierProcessor(counters);

        ItemIdValue id = ItemIdValueImpl.create("Q42", "foo");
        ItemDocument doc = ItemDocumentBuilder.forItemId(id)
                .withStatement(
                        StatementBuilder
                                .forSubjectAndProperty(id, PropertyIdValueImpl.create("P1", "bar"))
                                .withQualifier(Datamodel.makeValueSnak(PropertyIdValueImpl.create("P1", "bar"), Datamodel.makeStringValue("baz")))
                                .build()
                )
                .withStatement(
                        StatementBuilder
                                .forSubjectAndProperty(id, PropertyIdValueImpl.create("P1", "bar"))
                                .withQualifier(Datamodel.makeValueSnak(PropertyIdValueImpl.create("P1", "bar"), Datamodel.makeStringValue("baz")))
                                .withQualifier(Datamodel.makeValueSnak(PropertyIdValueImpl.create("P1", "bar"), Datamodel.makeStringValue("baz")))
                                .build()
                )
                .withStatement(
                        StatementBuilder
                                .forSubjectAndProperty(id, PropertyIdValueImpl.create("P100", "foo"))
                                .build()
                )
                .build();

        processor.processItemDocument( doc );

        this.assertCounter(counters, "qualifiers", 3);
    }
}
