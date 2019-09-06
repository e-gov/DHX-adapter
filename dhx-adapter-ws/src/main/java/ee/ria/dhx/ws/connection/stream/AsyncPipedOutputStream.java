package ee.ria.dhx.ws.connection.stream;

import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executor;

public class AsyncPipedOutputStream extends PipedOutputStream {

    private Executor executor;
    private PipedInputStream sink;

    public AsyncPipedOutputStream(Executor executor) {
        this.executor = executor;
    }

    @Override
    public synchronized void connect(PipedInputStream sink) throws IOException {
        super.connect(sink);
        this.sink = sink;
    }

    private void writeSuper(byte b[], int off, int len) throws IOException {
        super.write(b, off, len);
    }

    private boolean superWriteValidate(byte b[], int off, int len) throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        } else if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        boolean isSomethingToRead = len != 0;
        return isSomethingToRead;
    }

    @Async
    public void write(final byte b[], final int off, final int len) throws IOException {
        boolean isSomethingToRead = superWriteValidate(b, off, len);
        if (isSomethingToRead) {
            executor.execute(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    try {
                        writeSuper(b, off, len);
                    } catch (IOException ex) {
                        if (ex.getMessage().equals("Pipe closed")) {
                            return;
                        }
                        throw ex;
                    }
                }
            });
        }
    }
}
