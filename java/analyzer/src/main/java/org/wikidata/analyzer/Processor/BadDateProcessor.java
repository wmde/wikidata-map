package main.java.org.wikidata.analyzer.Processor;

import org.wikidata.wdtk.datamodel.interfaces.*;

import java.io.*;
import java.util.*;

/**
 * BadDateProcessor for wikidata-analysis
 *
 * @author Addshore
 */
public class BadDateProcessor implements EntityDocumentProcessor {

    private BufferedWriter writer1;
    private BufferedWriter writer2;

    private String gregorianCalendar = "http://www.wikidata.org/entity/Q1985727";
    private String julianCalendar = "http://www.wikidata.org/entity/Q1985786";

    public BadDateProcessor( BufferedWriter writer1, BufferedWriter writer2 ) {
        this.writer1 = writer1;
        this.writer2 = writer2;
    }

    @Override
    public void processItemDocument(ItemDocument item) {
        for (Iterator<Statement> statements = item.getAllStatements(); statements.hasNext(); ) {
            Statement statement = statements.next();
            Snak snak = statement.getClaim().getMainSnak();
            if (snak instanceof ValueSnak) {
                Value value = ((ValueSnak) snak).getValue();
                if (value instanceof TimeValue) {
                    TimeValue timeValue = (TimeValue) value;

                    //List1 - marked as Julian and are more precise than year
                    if(timeValue.getPreferredCalendarModel().equals( this.julianCalendar )
                            && timeValue.getPrecision() > 9) {
                        try {
                            this.writer1.write(statement.getStatementId() + "\n");
                        } catch (IOException e) {
                            System.out.println("Failed to write line to writer1");
                        }
                    }

                    //List2 - marked as gregorian, before 1584
                    if(timeValue.getPreferredCalendarModel().equals( this.gregorianCalendar )
                            && timeValue.getYear() < 1584) {
                        try {
                            this.writer2.write(statement.getStatementId() + "\n");
                        } catch (IOException e) {
                            System.out.println("Failed to write line to writer2");
                        }
                    }

                }
            }
        }
    }

    @Override
    public void processPropertyDocument(PropertyDocument property) {
    }

}