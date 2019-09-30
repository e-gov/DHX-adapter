package ee.ria.dhx.ws.connection;

import org.apache.http.entity.FileEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class DeleteOnCloseChunkedFileEntity extends FileEntity {
    public DeleteOnCloseChunkedFileEntity(File file) {
        super(file);
        setChunked(true);
    }

    public InputStream getContent() throws IOException {
        return Files.newInputStream(file.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
    }
}
