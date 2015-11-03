package test.java.org.wikidata.analyzer.Processor;

import junit.framework.TestCase;
import main.java.org.wikidata.analyzer.Processor.CounterProcessor;
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
 */
public class CounterProcessorTest extends TestCase {

    public void testProcessItemDocument() throws Exception {
        Map<String, Long> counters = new HashMap<>();
        CounterProcessor processor = new CounterProcessor(counters);

        ItemIdValue id = ItemIdValueImpl.create("Q42", "foo");
        ItemDocument doc = ItemDocumentBuilder.forItemId(id)
                .withLabel("label", "pt")
                .withDescription("desc", "sv")
                .withAlias("alias1", "de")
                .withAlias("alias2", "de")
                .withSiteLink("Title", "sitewiki")
                .withStatement(
                        StatementBuilder.forSubjectAndProperty(id, PropertyIdValueImpl.create("P1", "bar")).build()
                )
                .build();

        processor.processItemDocument( doc );

        assertEquals((long)counters.get("item"), (long)1 );
        assertEquals((long)counters.get("item.label"), (long)1 );
        assertEquals((long)counters.get("item.description"), (long)1 );
        assertEquals((long)counters.get("item.aliasgroup"), (long)1 );
        assertEquals((long)counters.get("item.alias"), (long)2 );
        assertEquals((long)counters.get("item.statement"), (long)1 );
        assertEquals((long)counters.get("item.sitelink"), (long)1 );

    }
}