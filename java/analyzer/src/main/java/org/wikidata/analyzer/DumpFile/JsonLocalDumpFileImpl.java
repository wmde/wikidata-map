package main.java.org.wikidata.analyzer.DumpFile;

import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.wmf.WmfDumpFile;
import org.wikidata.wdtk.util.CompressionType;
import org.wikidata.wdtk.util.DirectoryManager;
import org.wikidata.wdtk.util.DirectoryManagerImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implements the unimplemented class JsonLocalDumpFile from the ToolKit
 * This class is removed in a future version and this this may be rethought
 *
 * @author Addshore
 */
public class JsonLocalDumpFileImpl extends WmfDumpFile {

    public JsonLocalDumpFileImpl(String dateStamp) {
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

    @Override
    protected boolean fetchIsDone() {
        try {
            return this.getDirectoryManager().hasFile(this.getDumpFileName());
        } catch (IOException e) {
            return false;
        }
    }

    private DirectoryManager getDirectoryManager() throws IOException {
        return new DirectoryManagerImpl(
                System.getProperty("user.dir") +
                        File.separator + "data" +
                        File.separator + "dumpfiles" +
                        File.separator + "json-" + this.getDateStamp()
        );
    }
}
