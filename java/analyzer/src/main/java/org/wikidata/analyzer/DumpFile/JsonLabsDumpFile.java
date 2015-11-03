package main.java.org.wikidata.analyzer.DumpFile;

import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.wmf.WmfDumpFile;
import org.wikidata.wdtk.util.CompressionType;
import org.wikidata.wdtk.util.DirectoryManager;
import org.wikidata.wdtk.util.DirectoryManagerImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * @author Addshore
 */
public class JsonLabsDumpFile extends WmfDumpFile {

    public JsonLabsDumpFile(String dateStamp) {
        super(dateStamp, "wikidatawiki");
    }

    @Override
    public DumpContentType getDumpContentType() {
        return DumpContentType.JSON;
    }

    @Override
    public InputStream getDumpFileStream() throws IOException {
        return this.getDirectoryManager().getInputStreamForFile(
                this.getDumpFileName(),
                CompressionType.GZIP
        );
    }

    private String getDumpFileName() {
        return WmfDumpFile.getDumpFileName(DumpContentType.JSON, this.projectName, this.dateStamp);
    }

    @Override
    public void prepareDumpFile() throws IOException {
        // We need no preparation, it is either here or not
    }

    /**
     * @return boolean is the dump file requested on labs?
     */
    @Override
    protected boolean fetchIsDone() {
        try {
            return this.getDirectoryManager().hasFile(this.getDumpFileName());
        } catch (IOException e) {
            return false;
        }
    }

    private DirectoryManager getDirectoryManager() throws IOException {
        return new DirectoryManagerImpl(Paths.get("/data/scratch/wikidata/"), true);
    }
}
