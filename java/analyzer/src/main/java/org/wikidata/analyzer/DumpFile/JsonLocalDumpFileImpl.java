package main.java.org.wikidata.analyzer.DumpFile;

import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.wmf.WmfDumpFile;
import org.wikidata.wdtk.util.CompressionType;
import org.wikidata.wdtk.util.DirectoryManager;
import org.wikidata.wdtk.util.DirectoryManagerImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Implements the unimplemented class JsonLocalDumpFile from the ToolKit
 * This class is removed in a future version and this may be rethought
 *
 * @author Addshore
 */
public class JsonLocalDumpFileImpl extends WmfDumpFile {

    protected File dataDirectory;

    public JsonLocalDumpFileImpl(String dateStamp, File dataDirectory) {
        super(dateStamp, "wikidatawiki");
        this.dataDirectory = dataDirectory;
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
                Paths.get(
                        this.dataDirectory +
                                File.separator + "dumpfiles" +
                                File.separator + "json-" + this.getDateStamp()
                ),
                true
        );
    }
}
