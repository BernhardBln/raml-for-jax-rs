package org.raml.emitter;

import org.raml.model.RamlApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public class FileEmitter implements Emitter {

    private static final Logger logger = LoggerFactory.getLogger(FileEmitter.class);

    private final Path filePath;

    private FileEmitter(Path filePath) {
        this.filePath = filePath;
    }

    public static FileEmitter forFile(Path pathToFile) {
        checkNotNull(pathToFile);

        return new FileEmitter(pathToFile);
    }

    @Override
    public void emit(RamlApi api) throws RamlEmissionException {
        if (Files.isRegularFile(filePath)) {
            logger.warn("output file {} already exists, will be overwritten");
        }

        try (PrintWriter writer = printWriterOf(filePath)) {
            writeHeader(writer);
            writeTitle(writer, api.getTitle());
            writeVersion(writer, api.getVersion());
            writeBaseUri(writer, api.getBaseUri());
        } catch (IOException e) {
            throw new RamlEmissionException(String.format("unable to successfully output raml to %s", filePath), e);
        }
    }

    private void writeBaseUri(PrintWriter writer, String baseUri) {
        writer.printf("baseUri: %s\n", baseUri);
    }

    private void writeVersion(PrintWriter writer, String version) {
        writer.printf("version: %s\n", version);
    }

    private void writeTitle(PrintWriter writer, String title) {
        writer.printf("title: %s\n", title);
    }

    private void writeHeader(PrintWriter writer) {
        writer.println("#%RAML 1.0");
    }

    private static PrintWriter printWriterOf(Path path) throws IOException {
        return new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8));
    }
}
