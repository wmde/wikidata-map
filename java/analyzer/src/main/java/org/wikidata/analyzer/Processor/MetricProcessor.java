package main.java.org.wikidata.analyzer.Processor;

import com.google.common.collect.Iterators;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.*;

/**
 * @author Addshore
 */
public class MetricProcessor implements EntityDocumentProcessor {

    private Map<String, Long> counters;

    private Map<String, String> wikimedias = new HashMap<>();

    private List<String> referenceProperties = new ArrayList<>();

    public MetricProcessor(Map<String, Long> counters) {
        this.counters = counters;
        this.populateWikimedias();
        this.populateReferenceProperties();
    }

    /**
     * The below list was generated using a SPARQL query.
     * FIXME: there is probably a better way to store, manage and update this....
     */
    private void populateWikimedias() {
        this.wikimedias.put( "Q4783991", "cewiki" );
        this.wikimedias.put( "Q4925786", "ndswiki" );
        this.wikimedias.put( "Q5652665", "avwiki" );
        this.wikimedias.put( "Q6112922", "hakwiki" );
        this.wikimedias.put( "Q6125437", "ganwiki" );
        this.wikimedias.put( "Q6167360", "iewiki" );
        this.wikimedias.put( "Q6587084", "lbewiki" );
        this.wikimedias.put( "Q7102897", "orwiki" );
        this.wikimedias.put( "Q8042979", "fowiki" );
        this.wikimedias.put( "Q8075204", "zuwiki" );
        this.wikimedias.put( "Q8558731", "akwiki" );
        this.wikimedias.put( "Q8558960", "angwiki" );
        this.wikimedias.put( "Q8559119", "aswiki" );
        this.wikimedias.put( "Q8559737", "bmwiki" );
        this.wikimedias.put( "Q8560590", "newiki" );
        this.wikimedias.put( "Q8561147", "dsbwiki" );
        this.wikimedias.put( "Q8561277", "bhwiki" );
        this.wikimedias.put( "Q8561332", "biwiki" );
        this.wikimedias.put( "Q8561415", "bxrwiki" );
        this.wikimedias.put( "Q8561491", "chywiki" );
        this.wikimedias.put( "Q8561552", "nywiki" );
        this.wikimedias.put( "Q8561582", "crwiki" );
        this.wikimedias.put( "Q8561662", "dzwiki" );
        this.wikimedias.put( "Q8561870", "bclwiki" );
        this.wikimedias.put( "Q8562097", "eewiki" );
        this.wikimedias.put( "Q8562272", "gdwiki" );
        this.wikimedias.put( "Q8562481", "hifwiki" );
        this.wikimedias.put( "Q8562502", "fjwiki" );
        this.wikimedias.put( "Q8562529", "frpwiki" );
        this.wikimedias.put( "Q8562927", "ffwiki" );
        this.wikimedias.put( "Q8563136", "gotwiki" );
        this.wikimedias.put( "Q8563393", "hawiki" );
        this.wikimedias.put( "Q8563635", "igwiki" );
        this.wikimedias.put( "Q8563685", "ilowiki" );
        this.wikimedias.put( "Q8563863", "ikwiki" );
        this.wikimedias.put( "Q8564352", "kabwiki" );
        this.wikimedias.put( "Q8565447", "kswiki" );
        this.wikimedias.put( "Q8565463", "kgwiki" );
        this.wikimedias.put( "Q8565476", "kiwiki" );
        this.wikimedias.put( "Q8565518", "rwwiki" );
        this.wikimedias.put( "Q8565742", "rnwiki" );
        this.wikimedias.put( "Q8565801", "kwwiki" );
        this.wikimedias.put( "Q8566298", "lnwiki" );
        this.wikimedias.put( "Q8566311", "jbowiki" );
        this.wikimedias.put( "Q8566347", "lgwiki" );
        this.wikimedias.put( "Q8566503", "gvwiki" );
        this.wikimedias.put( "Q8568791", "mwlwiki" );
        this.wikimedias.put( "Q8569757", "nvwiki" );
        this.wikimedias.put( "Q8569951", "arcwiki" );
        this.wikimedias.put( "Q8570048", "pihwiki" );
        this.wikimedias.put( "Q8570353", "novwiki" );
        this.wikimedias.put( "Q8570425", "omwiki" );
        this.wikimedias.put( "Q8570791", "piwiki" );
        this.wikimedias.put( "Q8571143", "rmywiki" );
        this.wikimedias.put( "Q8571427", "smwiki" );
        this.wikimedias.put( "Q8571487", "sgwiki" );
        this.wikimedias.put( "Q8571809", "snwiki" );
        this.wikimedias.put( "Q8571840", "sdwiki" );
        this.wikimedias.put( "Q8571954", "siwiki" );
        this.wikimedias.put( "Q8572132", "sowiki" );
        this.wikimedias.put( "Q8572199", "stwiki" );
        this.wikimedias.put( "Q8575385", "tetwiki" );
        this.wikimedias.put( "Q8575467", "tiwiki" );
        this.wikimedias.put( "Q8575674", "tswiki" );
        this.wikimedias.put( "Q8575782", "tumwiki" );
        this.wikimedias.put( "Q8575885", "twwiki" );
        this.wikimedias.put( "Q8575930", "cbk_zamwiki" );
        this.wikimedias.put( "Q8576190", "chwiki" );
        this.wikimedias.put( "Q8576237", "chrwiki" );
        this.wikimedias.put( "Q8577029", "vewiki" );
        this.wikimedias.put( "Q8582589", "wowiki" );
        this.wikimedias.put( "Q8669146", "frrwiki" );
        this.wikimedias.put( "Q8937989", "be_x_oldwiki" );
        this.wikimedias.put( "Q12265494", "pagwiki" );
        this.wikimedias.put( "Q13230970", "nsowiki" );
        this.wikimedias.put( "Q13231253", "kbdwiki" );
        this.wikimedias.put( "Q13358221", "pflwiki" );
        this.wikimedias.put( "Q14948450", "tyvwiki" );
        this.wikimedias.put( "Q18508969", "maiwiki" );
        this.wikimedias.put( "Q20442276", "lrcwiki" );
        this.wikimedias.put( "Q20726662", "gomwiki" );
        this.wikimedias.put( "Q1132977", "wawiki" );
        this.wikimedias.put( "Q1147071", "anwiki" );
        this.wikimedias.put( "Q1148240", "yowiki" );
        this.wikimedias.put( "Q1154741", "kuwiki" );
        this.wikimedias.put( "Q1154766", "iowiki" );
        this.wikimedias.put( "Q1178461", "mdfwiki" );
        this.wikimedias.put( "Q1190962", "zh_yuewiki" );
        this.wikimedias.put( "Q1211233", "alswiki" );
        this.wikimedias.put( "Q1249553", "krcwiki" );
        this.wikimedias.put( "Q1287192", "bpywiki" );
        this.wikimedias.put( "Q1291627", "newwiki" );
        this.wikimedias.put( "Q1377618", "quwiki" );
        this.wikimedias.put( "Q1378484", "zh_classicalwiki" );
        this.wikimedias.put( "Q1444686", "scowiki" );
        this.wikimedias.put( "Q1551807", "plwiki" );
        this.wikimedias.put( "Q1574617", "nds_nlwiki" );
        this.wikimedias.put( "Q1585232", "fiu_vrowiki" );
        this.wikimedias.put( "Q1648786", "warwiki" );
        this.wikimedias.put( "Q1754193", "pawiki" );
        this.wikimedias.put( "Q1961887", "barwiki" );
        this.wikimedias.put( "Q1968379", "yiwiki" );
        this.wikimedias.put( "Q1975217", "hywiki" );
        this.wikimedias.put( "Q2029239", "xmfwiki" );
        this.wikimedias.put( "Q2073394", "roa_rupwiki" );
        this.wikimedias.put( "Q2081526", "uzwiki" );
        this.wikimedias.put( "Q2091593", "bowiki" );
        this.wikimedias.put( "Q2111591", "zeawiki" );
        this.wikimedias.put( "Q2328409", "liwiki" );
        this.wikimedias.put( "Q2349453", "nnwiki" );
        this.wikimedias.put( "Q2374285", "arzwiki" );
        this.wikimedias.put( "Q2402143", "hsbwiki" );
        this.wikimedias.put( "Q2587255", "sawiki" );
        this.wikimedias.put( "Q2602203", "fywiki" );
        this.wikimedias.put( "Q2732019", "miwiki" );
        this.wikimedias.put( "Q2742472", "tgwiki" );
        this.wikimedias.put( "Q2744155", "nahwiki" );
        this.wikimedias.put( "Q2913253", "ltgwiki" );
        this.wikimedias.put( "Q2983979", "bjnwiki" );
        this.wikimedias.put( "Q2996321", "ru_sibwiki" );
        this.wikimedias.put( "Q2998037", "mnwiki" );
        this.wikimedias.put( "Q3025527", "amwiki" );
        this.wikimedias.put( "Q3025736", "pdcwiki" );
        this.wikimedias.put( "Q3026819", "rmwiki" );
        this.wikimedias.put( "Q3046353", "pmswiki" );
        this.wikimedias.put( "Q3111179", "cowiki" );
        this.wikimedias.put( "Q3112631", "towiki" );
        this.wikimedias.put( "Q3123304", "mgwiki" );
        this.wikimedias.put( "Q3180091", "mtwiki" );
        this.wikimedias.put( "Q3180306", "guwiki" );
        this.wikimedias.put( "Q3181422", "knwiki" );
        this.wikimedias.put( "Q3181928", "extwiki" );
        this.wikimedias.put( "Q3239456", "zh_min_nanwiki" );
        this.wikimedias.put( "Q3311132", "zawiki" );
        this.wikimedias.put( "Q3432470", "sswiki" );
        this.wikimedias.put( "Q3477935", "jvwiki" );
        this.wikimedias.put( "Q3486726", "mrwiki" );
        this.wikimedias.put( "Q3568035", "abwiki" );
        this.wikimedias.put( "Q3568038", "vlswiki" );
        this.wikimedias.put( "Q3568039", "furwiki" );
        this.wikimedias.put( "Q3568040", "stqwiki" );
        this.wikimedias.put( "Q3568041", "kshwiki" );
        this.wikimedias.put( "Q3568042", "klwiki" );
        this.wikimedias.put( "Q3568043", "hawwiki" );
        this.wikimedias.put( "Q3568044", "kmwiki" );
        this.wikimedias.put( "Q3568045", "lowiki" );
        this.wikimedias.put( "Q3568046", "lijwiki" );
        this.wikimedias.put( "Q3568048", "mznwiki" );
        this.wikimedias.put( "Q3568049", "mowiki" );
        this.wikimedias.put( "Q3568051", "nrmwiki" );
        this.wikimedias.put( "Q3568053", "pcdwiki" );
        this.wikimedias.put( "Q3568054", "pswiki" );
        this.wikimedias.put( "Q3568056", "papwiki" );
        this.wikimedias.put( "Q3568059", "scwiki" );
        this.wikimedias.put( "Q3568060", "srnwiki" );
        this.wikimedias.put( "Q3568061", "tywiki" );
        this.wikimedias.put( "Q3568062", "roa_tarawiki" );
        this.wikimedias.put( "Q3568063", "tnwiki" );
        this.wikimedias.put( "Q3568065", "xhwiki" );
        this.wikimedias.put( "Q3568066", "emlwiki" );
        this.wikimedias.put( "Q3568069", "bat_smgwiki" );
        this.wikimedias.put( "Q3696028", "pnbwiki" );
        this.wikimedias.put( "Q3753095", "nawiki" );
        this.wikimedias.put( "Q3756269", "csbwiki" );
        this.wikimedias.put( "Q3756562", "ladwiki" );
        this.wikimedias.put( "Q3757068", "iawiki" );
        this.wikimedias.put( "Q3807895", "gnwiki" );
        this.wikimedias.put( "Q3826575", "aywiki" );
        this.wikimedias.put( "Q3913095", "iuwiki" );
        this.wikimedias.put( "Q3913160", "lmowiki" );
        this.wikimedias.put( "Q3944107", "glkwiki" );
        this.wikimedias.put( "Q3957795", "acewiki" );
        this.wikimedias.put( "Q4077512", "map_bmswiki" );
        this.wikimedias.put( "Q4097773", "bugwiki" );
        this.wikimedias.put( "Q4107346", "vepwiki" );
        this.wikimedias.put( "Q4115441", "sewiki" );
        this.wikimedias.put( "Q4115463", "ckbwiki" );
        this.wikimedias.put( "Q4210231", "xalwiki" );
        this.wikimedias.put( "Q4296423", "minwiki" );
        this.wikimedias.put( "Q4372058", "pntwiki" );
        this.wikimedias.put( "Q4614845", "mywiki" );
        this.wikimedias.put( "Q328", "enwiki" );
        this.wikimedias.put( "Q8447", "frwiki" );
        this.wikimedias.put( "Q8449", "eswiki" );
        this.wikimedias.put( "Q10000", "nlwiki" );
        this.wikimedias.put( "Q11913", "bgwiki" );
        this.wikimedias.put( "Q11918", "elwiki" );
        this.wikimedias.put( "Q11920", "itwiki" );
        this.wikimedias.put( "Q11921", "ptwiki" );
        this.wikimedias.put( "Q12237", "lawiki" );
        this.wikimedias.put( "Q14380", "slwiki" );
        this.wikimedias.put( "Q17985", "kowiki" );
        this.wikimedias.put( "Q30239", "zhwiki" );
        this.wikimedias.put( "Q38288", "diqwiki" );
        this.wikimedias.put( "Q45041", "lezwiki" );
        this.wikimedias.put( "Q48183", "dewiki" );
        this.wikimedias.put( "Q48952", "fawiki" );
        this.wikimedias.put( "Q53464", "huwiki" );
        this.wikimedias.put( "Q58172", "kkwiki" );
        this.wikimedias.put( "Q58209", "bawiki" );
        this.wikimedias.put( "Q58215", "cvwiki" );
        this.wikimedias.put( "Q58251", "azwiki" );
        this.wikimedias.put( "Q58255", "trwiki" );
        this.wikimedias.put( "Q58679", "shwiki" );
        this.wikimedias.put( "Q58781", "ruewiki" );
        this.wikimedias.put( "Q60786", "crhwiki" );
        this.wikimedias.put( "Q60799", "kywiki" );
        this.wikimedias.put( "Q60819", "ttwiki" );
        this.wikimedias.put( "Q60856", "ugwiki" );
        this.wikimedias.put( "Q79633", "gagwiki" );
        this.wikimedias.put( "Q79636", "kaawiki" );
        this.wikimedias.put( "Q155214", "idwiki" );
        this.wikimedias.put( "Q169514", "svwiki" );
        this.wikimedias.put( "Q175482", "fiwiki" );
        this.wikimedias.put( "Q177837", "jawiki" );
        this.wikimedias.put( "Q181163", "dawiki" );
        this.wikimedias.put( "Q190551", "eowiki" );
        this.wikimedias.put( "Q191168", "cswiki" );
        this.wikimedias.put( "Q191769", "nowiki" );
        this.wikimedias.put( "Q192582", "skwiki" );
        this.wikimedias.put( "Q199693", "cawiki" );
        this.wikimedias.put( "Q199698", "ukwiki" );
        this.wikimedias.put( "Q199700", "arwiki" );
        this.wikimedias.put( "Q199864", "rowiki" );
        this.wikimedias.put( "Q199913", "hewiki" );
        this.wikimedias.put( "Q200060", "etwiki" );
        this.wikimedias.put( "Q200180", "viwiki" );
        this.wikimedias.put( "Q200183", "simplewiki" );
        this.wikimedias.put( "Q200386", "srwiki" );
        this.wikimedias.put( "Q202472", "ltwiki" );
        this.wikimedias.put( "Q203488", "hrwiki" );
        this.wikimedias.put( "Q206855", "ruwiki" );
        this.wikimedias.put( "Q207260", "euwiki" );
        this.wikimedias.put( "Q208533", "sqwiki" );
        this.wikimedias.put( "Q221444", "udmwiki" );
        this.wikimedias.put( "Q225594", "sahwiki" );
        this.wikimedias.put( "Q226150", "oswiki" );
        this.wikimedias.put( "Q427715", "bnwiki" );
        this.wikimedias.put( "Q511754", "tkwiki" );
        this.wikimedias.put( "Q547271", "cuwiki" );
        this.wikimedias.put( "Q565074", "thwiki" );
        this.wikimedias.put( "Q571001", "tpiwiki" );
        this.wikimedias.put( "Q588620", "pamwiki" );
        this.wikimedias.put( "Q595628", "ocwiki" );
        this.wikimedias.put( "Q714826", "vowiki" );
        this.wikimedias.put( "Q718394", "iswiki" );
        this.wikimedias.put( "Q722040", "hiwiki" );
        this.wikimedias.put( "Q722243", "swwiki" );
        this.wikimedias.put( "Q728945", "lvwiki" );
        this.wikimedias.put( "Q766705", "afwiki" );
        this.wikimedias.put( "Q824297", "mhrwiki" );
        this.wikimedias.put( "Q837615", "cebwiki" );
        this.wikimedias.put( "Q841208", "glwiki" );
        this.wikimedias.put( "Q842341", "mkwiki" );
        this.wikimedias.put( "Q844491", "tawiki" );
        this.wikimedias.put( "Q845993", "mswiki" );
        this.wikimedias.put( "Q846630", "cdowiki" );
        this.wikimedias.put( "Q846871", "brwiki" );
        this.wikimedias.put( "Q848046", "tewiki" );
        this.wikimedias.put( "Q848525", "cywiki" );
        this.wikimedias.put( "Q848974", "kawiki" );
        this.wikimedias.put( "Q856881", "myvwiki" );
        this.wikimedias.put( "Q874555", "mlwiki" );
        this.wikimedias.put( "Q875631", "gawiki" );
        this.wikimedias.put( "Q877583", "bewiki" );
        this.wikimedias.put( "Q877685", "tlwiki" );
        this.wikimedias.put( "Q925661", "kvwiki" );
        this.wikimedias.put( "Q928808", "dvwiki" );
        this.wikimedias.put( "Q940309", "szlwiki" );
        this.wikimedias.put( "Q950058", "lbwiki" );
        this.wikimedias.put( "Q966609", "suwiki" );
        this.wikimedias.put( "Q1034940", "mrjwiki" );
        this.wikimedias.put( "Q1047829", "bswiki" );
        this.wikimedias.put( "Q1047851", "napwiki" );
        this.wikimedias.put( "Q1055841", "vecwiki" );
        this.wikimedias.put( "Q1058430", "scnwiki" );
        this.wikimedias.put( "Q1066461", "htwiki" );
        this.wikimedias.put( "Q1067878", "urwiki" );
        this.wikimedias.put( "Q1071918", "astwiki" );
        this.wikimedias.put( "Q1110233", "wuuwiki" );
        this.wikimedias.put( "Q1116066", "koiwiki" );
    }

    /**
     * The below list was generated using a SPARQL query
     * FIXME: there is probably a better way to store, manage and update this....
     */
    private void populateReferenceProperties() {
        this.referenceProperties.add("P143");
        this.referenceProperties.add("P1480");
        this.referenceProperties.add("P1683");
        this.referenceProperties.add("P248");
        this.referenceProperties.add("P813");
        this.referenceProperties.add("P854");
        this.referenceProperties.add("P887");
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
            this.processStatementDocument(document);
        }
    }

    public void processPropertyDocument(PropertyDocument document) {
        //Look for nothing
    }

    private void processStatementDocument(StatementDocument document) {
        for (Iterator<Statement> statementIterator = document.getAllStatements(); statementIterator.hasNext(); ) {
            Statement statement = statementIterator.next();
            processStatement(statement);
        }
    }

    private void processStatement(Statement statement) {
        Snak mainSnak = statement.getClaim().getMainSnak();
        String propertyString = mainSnak.getPropertyId().getId();

        this.increment("qualifiers", Iterators.size(statement.getClaim().getAllQualifiers()));
        this.increment("references", statement.getReferences().size());

        if( statement.getReferences().size() == 0 ) {
            this.increment("statements.unreferenced");
        } else {
            this.increment("statements.referenced");
        }

        for (Reference reference : statement.getReferences()) {
            processReference(reference);
        }
    }

    private void processReference(Reference reference) {
        this.increment("references.snaks", Iterators.size(reference.getAllSnaks()));

        for( Iterator<Snak> snaks = reference.getAllSnaks(); snaks.hasNext(); ) {
            Snak snak = snaks.next();
            processReferenceSnak(snak);
        }
    }

    private void processReferenceSnak(Snak snak) {
        String propertyId = snak.getPropertyId().getId();

        //Only count the counts of /approved/ properties
        if( this.referenceProperties.contains( propertyId ) ) {
            this.increment("references.snaks.prop." + propertyId);
        }

        if (snak instanceof ValueSnak) {
            this.increment("references.snaks.type.value");
            this.processReferenceValueSnak((ValueSnak)snak);
        } else if (snak instanceof SomeValueSnak) {
            this.increment("references.snaks.type.somevalue");
        } else if (snak instanceof NoValueSnak) {
            this.increment("references.snaks.type.novalue");
        }
    }

    private void processReferenceValueSnak(ValueSnak snak) {
        String propertyId = snak.getPropertyId().getId();
        //Look for snaks indicating a Wikimedia reference
        //Note: P143 (imported from), P248 (stated in)
        if( propertyId.equals( "P143" ) || propertyId.equals("P248") ) {
            //Note: must always be an EntityIdValue for the properties above
            EntityIdValue entityIdValue = (EntityIdValue) snak.getValue();
            if( this.wikimedias.containsKey( entityIdValue.getId() ) ) {
                this.increment("references.snaks.wm");
                this.increment("references.snaks.wm." + this.wikimedias.get( entityIdValue.getId() ));
            }
        }
    }

}
