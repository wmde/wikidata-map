package test.java.org.wikidata.analyzer.Processor;

import junit.framework.TestCase;
import main.java.org.wikidata.analyzer.Processor.BadDateProcessor;
import org.easymock.EasyMock;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyIdValueImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;

import java.io.*;

/**
 * @author Addshore
 */
public class BadDateProcessorTest extends TestCase {

    public void testProcessItemDocument_constructWrites() throws Exception {
        Writer writer1 = EasyMock.createMock(Writer.class);
        Writer writer2 = EasyMock.createMock(Writer.class);

        writer1.write("Dates marked as Julian that are more precise than year\n----\n");
        EasyMock.expectLastCall();
        writer2.write("Dates marked as gregorian, before 1584\n----\n");
        EasyMock.expectLastCall();

        EasyMock.replay(writer1);
        EasyMock.replay(writer2);

        new BadDateProcessor(writer1, writer2);

        EasyMock.verify(writer1);
        EasyMock.verify(writer2);
    }

    public void testProcessItemDocument_noMatches() throws Exception {
        Writer writer1 = EasyMock.createMock(Writer.class);
        Writer writer2 = EasyMock.createMock(Writer.class);

        writer1.write("Dates marked as Julian that are more precise than year\n----\n");
        EasyMock.expectLastCall();
        writer2.write("Dates marked as gregorian, before 1584\n----\n");
        EasyMock.expectLastCall();

        EasyMock.replay(writer1);
        EasyMock.replay(writer2);

        EntityDocumentProcessor proc = new BadDateProcessor(writer1, writer2);

        ItemIdValue id = ItemIdValueImpl.create("Q42", "foo");
        ItemDocument doc = ItemDocumentBuilder.forItemId(id)
                .withStatement(
                        StatementBuilder.forSubjectAndProperty(id, PropertyIdValueImpl.create("P1", "bar"))
                                .withId("someStatementGuid")
                                .withValue(Datamodel.makeTimeValue(2015, (byte) 1, (byte) 1, TimeValue.CM_GREGORIAN_PRO))
                                .build()
                )
                .build();

        proc.processItemDocument(doc);

        EasyMock.verify(writer1);
        EasyMock.verify(writer2);
    }

    public void testProcessItemDocument_julianMorePreciseThanYear() throws Exception {
        Writer writer1 = EasyMock.createMock(Writer.class);
        Writer writer2 = EasyMock.createMock(Writer.class);

        writer1.write("Dates marked as Julian that are more precise than year\n----\n");
        EasyMock.expectLastCall();
        writer2.write("Dates marked as gregorian, before 1584\n----\n");
        EasyMock.expectLastCall();
        writer1.write("someStatementGuid\n");
        EasyMock.expectLastCall();

        EasyMock.replay(writer1);
        EasyMock.replay(writer2);

        EntityDocumentProcessor proc = new BadDateProcessor(writer1, writer2);

        ItemIdValue id = ItemIdValueImpl.create("Q42", "foo");
        ItemDocument doc = ItemDocumentBuilder.forItemId(id)
                .withStatement(
                        StatementBuilder.forSubjectAndProperty(id, PropertyIdValueImpl.create("P1", "bar"))
                                .withId("someStatementGuid")
                                .withValue(Datamodel.makeTimeValue(2015, (byte) 1, (byte) 1, TimeValue.CM_JULIAN_PRO))
                                .build()
                )
                .build();

        proc.processItemDocument(doc);

        EasyMock.verify(writer1);
        EasyMock.verify(writer2);
    }

    public void testProcessItemDocument_gregorianBefore1584() throws Exception {
        Writer writer1 = EasyMock.createMock(Writer.class);
        Writer writer2 = EasyMock.createMock(Writer.class);

        writer1.write("Dates marked as Julian that are more precise than year\n----\n");
        EasyMock.expectLastCall();
        writer2.write("Dates marked as gregorian, before 1584\n----\n");
        EasyMock.expectLastCall();
        writer2.write("someStatementGuid\n");
        EasyMock.expectLastCall();

        EasyMock.replay(writer1);
        EasyMock.replay(writer2);

        EntityDocumentProcessor proc = new BadDateProcessor(writer1, writer2);

        ItemIdValue id = ItemIdValueImpl.create("Q42", "foo");
        ItemDocument doc = ItemDocumentBuilder.forItemId(id)
                .withStatement(
                        StatementBuilder.forSubjectAndProperty(id, PropertyIdValueImpl.create("P1", "bar"))
                                .withId("someStatementGuid")
                                .withValue(Datamodel.makeTimeValue(1583, (byte) 1, (byte) 1, TimeValue.CM_GREGORIAN_PRO))
                                .build()
                )
                .build();

        proc.processItemDocument(doc);

        EasyMock.verify(writer1);
        EasyMock.verify(writer2);
    }

}