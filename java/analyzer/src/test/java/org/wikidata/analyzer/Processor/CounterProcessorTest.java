package test.java.org.wikidata.analyzer.Processor;

import junit.framework.TestCase;
import main.java.org.wikidata.analyzer.Processor.CounterProcessor;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.PropertyDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
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
public class CounterProcessorTest extends TestCase {

    private void assertCounter( Map<String, Long> counters, String counter, int expected ) {
        assertTrue( "Assert counter name exists '" + counter + "'", counters.containsKey( counter ) );
        assertEquals( "Assert counter '" + counter + "'value correct", (long)counters.get( counter ), (long)expected );
    }

    public void testProcessItemDocument() throws Exception {
        Map<String, Long> counters = new HashMap<>();
        CounterProcessor processor = new CounterProcessor(counters);

        ItemIdValue id = ItemIdValueImpl.create("Q42", "foo");
        ItemDocument doc = ItemDocumentBuilder.forItemId(id)
                .withLabel("label", "pt")
                .withDescription("desc", "sv")
                .withDescription("desc", "de")
                .withAlias("alias1", "de")
                .withAlias("alias2", "de")
                .withSiteLink("Title", "sitewiki")
                .withStatement(
                        StatementBuilder.forSubjectAndProperty(id, PropertyIdValueImpl.create("P1", "bar")).build()
                )
                .build();

        processor.processItemDocument( doc );

        this.assertCounter(counters, "item.total", 1);
        this.assertCounter(counters, "item.label.total", 1 );
        this.assertCounter(counters, "item.label.perlang.pt", 1 );
        this.assertCounter(counters, "item.label.perentity.1", 1 );
        this.assertCounter(counters, "item.description.total", 2 );
        this.assertCounter(counters, "item.description.perlang.sv", 1 );
        this.assertCounter(counters, "item.description.perlang.de", 1 );
        this.assertCounter(counters, "item.description.perentity.2", 1 );
        this.assertCounter(counters, "item.aliasgroup.total", 1 );
        this.assertCounter(counters, "item.aliasgroup.perentity.1", 1 );
        this.assertCounter(counters, "item.aliasgroup.perlang.de", 1 );
        this.assertCounter(counters, "item.alias.total", 2 );
        this.assertCounter(counters, "item.alias.perlang.de", 2 );
        this.assertCounter(counters, "item.statement.total", 1 );
        //TODO lower case rank?
        this.assertCounter(counters, "item.statement.rank.NORMAL.total", 1 );
        this.assertCounter(counters, "item.statement.mainsnak.total", 1 );
        //TODO test an actual value snak?
        this.assertCounter(counters, "item.statement.mainsnak.type.somevalue.total", 1 );
        this.assertCounter(counters, "item.sitelink.total", 1 );
        this.assertCounter(counters, "item.sitelink.perentity.1", 1 );
        this.assertCounter(counters, "item.sitelink.persite.sitewiki", 1 );
    }

    public void testProcessPropertyDocument() throws Exception {
        Map<String, Long> counters = new HashMap<>();
        CounterProcessor processor = new CounterProcessor(counters);

        PropertyIdValue id = PropertyIdValueImpl.create("P42", "foo");
        PropertyDocument doc = PropertyDocumentBuilder.forPropertyIdAndDatatype(id, Datamodel.makeDatatypeIdValue("dtId1"))
                .withLabel("label", "pt")
                .withDescription("desc", "sv")
                .withAlias("alias1", "de")
                .withAlias("alias2", "de")
                .withStatement(
                        StatementBuilder.forSubjectAndProperty(id, PropertyIdValueImpl.create("P1", "bar")).build()
                )
                .build();

        processor.processPropertyDocument(doc);

        this.assertCounter(counters, "property.total", 1 );
        this.assertCounter(counters, "property.label.total", 1 );
        this.assertCounter(counters, "property.description.total", 1 );
        this.assertCounter(counters, "property.aliasgroup.total", 1 );
        this.assertCounter(counters, "property.alias.total", 2 );
        this.assertCounter(counters, "property.statement.total", 1 );
        this.assertCounter(counters, "property.datatype.dtId1.total", 1 );
    }
}