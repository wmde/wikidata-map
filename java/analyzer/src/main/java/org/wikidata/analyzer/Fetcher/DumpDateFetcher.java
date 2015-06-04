package main.java.org.wikidata.analyzer.Fetcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Addshore
 */
public class DumpDateFetcher {

    /**
     * This presumes that the newest dump is at the bottom of the list (ordered by name)
     *
     * @return String in format yyyymmdd eg. 20150525
     * @throws IOException
     */
    public String getLatestOnlineDumpDate() throws IOException {
        String html = this.getJsonDumpsPageHtml();
        Document doc = Jsoup.parse(html);
        Element finalLink = doc.select("a").last();
        String fileName = finalLink.html();
        return fileName.substring(0, fileName.length() - 8);// remove 8 chars
    }

    /**
     * @return String html of the json dumps page for wikidata
     * @throws IOException
     */
    private String getJsonDumpsPageHtml() throws IOException {
        URL url = new URL("http://dumps.wikimedia.org/other/wikidata");
        URLConnection con = url.openConnection();
        Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
        Matcher m = p.matcher(con.getContentType());
        /* If Content-Type doesn't match this pre-conception, choose default and hope for the best. */
        String charset = m.matches() ? m.group(1) : "ISO-8859-1";
        Reader r = new InputStreamReader(con.getInputStream(), charset);
        StringBuilder buf = new StringBuilder();
        while (true) {
            int ch = r.read();
            if (ch < 0)
                break;
            buf.append((char) ch);
        }
        return buf.toString();
    }

}
