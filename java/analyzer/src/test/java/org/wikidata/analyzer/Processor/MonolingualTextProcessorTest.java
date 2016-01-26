package test.java.org.wikidata.analyzer.Processor;

import junit.framework.TestCase;
import main.java.org.wikidata.analyzer.Processor.MonolingualTextProcessor;
import org.wikidata.wdtk.datamodel.helpers.*;
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyIdValueImpl;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Addshore
 */
public class MonolingualTextProcessorTest extends TestCase {

    private void assertCounter( Map<String, Long> counters, String counter, int expected ) {
        assertTrue( "Assert counter name exists '" + counter + "'", counters.containsKey( counter ) );
        assertEquals( "Assert counter '" + counter + "'value correct", (long)expected, (long)counters.get( counter ) );
    }

    public void testProcessItemDocument() throws Exception {
        Map<String, Long> counters = new HashMap<>();
        MonolingualTextProcessor processor = new MonolingualTextProcessor(counters);

        ItemIdValue itemId = ItemIdValueImpl.create("Q42", "foo");
        ItemDocument itemDocument = ItemDocumentBuilder.forItemId(itemId)
                .withStatement(
                        StatementBuilder
                                .forSubjectAndProperty(itemId, PropertyIdValueImpl.create("P1", "bar"))
                                .withValue(Datamodel.makeMonolingualTextValue("text", "en"))
                                .withQualifier(Datamodel.makeValueSnak(PropertyIdValueImpl.create("P1", "bar"), Datamodel.makeMonolingualTextValue("text", "de")))
                                .withQualifier(Datamodel.makeValueSnak(PropertyIdValueImpl.create("P1", "bar"), Datamodel.makeMonolingualTextValue("text", "fr")))
                                .withReference(ReferenceBuilder.newInstance().withPropertyValue(
                                        PropertyIdValueImpl.create("P2", "Foo"),
                                        Datamodel.makeMonolingualTextValue("text", "fr")
                                ).build())
                                .withReference(ReferenceBuilder.newInstance().withPropertyValue(
                                        PropertyIdValueImpl.create("P2", "Foo"),
                                        Datamodel.makeMonolingualTextValue("text", "pt")
                                ).build())
                                .build()
                )
                .build();

        PropertyIdValue propertyId = PropertyIdValueImpl.create("P42", "foo");
        PropertyDocument propertyDocument = PropertyDocumentBuilder.forPropertyIdAndDatatype(propertyId, "foo")
                .withStatement(
                        StatementBuilder
                                .forSubjectAndProperty(propertyId, PropertyIdValueImpl.create("P1", "bar"))
                                .withValue(Datamodel.makeMonolingualTextValue("text", "pt"))
                                .build()
                )
                .build();

        processor.processItemDocument( itemDocument );
        processor.processPropertyDocument( propertyDocument );

        this.assertCounter(counters, "en", 1);
        this.assertCounter(counters, "de", 1);
        this.assertCounter(counters, "fr", 2 );
        this.assertCounter(counters, "pt", 2 );
    }

}