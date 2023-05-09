package eu.ebrains.kg.zipper.controller;

import eu.ebrains.kg.zipper.models.Container;
import eu.ebrains.kg.zipper.models.FileReference;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

public abstract class Loader {
    abstract public List<FileReference> getUrls(Container container);

    protected static RequestHeadersSpec<?> withAuth(RequestHeadersSpec<?> spec, Optional<String> bearerToken) {
        if (bearerToken.isPresent()) {
            return spec.header("Authorization", bearerToken.get());
        } else {
            return spec;
        }
    }

    public Boolean download(FileReference fileReference, Container container, ZipOutputStream zipOutputStream, Logger logger) throws IOException, InterruptedException, URISyntaxException {
        if (fileReference.isIgnore()) {
            return false;
        }
        int maxRetries = 3;
        boolean isSuccess = false;

        zipOutputStream.putNextEntry(new ZipEntry(fileReference.getRelativeFilePath(container)));
        URI encodedUrl = getEncodedURI(fileReference.getUrl());
        while (maxRetries > 0 && !isSuccess) {
            try {
                final Flux<DataBuffer> dataBufferFlux = getDataBufferFlux(encodedUrl, container.getBearerToken());
                DataBufferUtils.write(dataBufferFlux, zipOutputStream).map(DataBufferUtils::release).blockLast();
                logger.info("Downloaded {}", fileReference.getUrl());
                isSuccess = true;
            } catch (RuntimeException exc) { // DataBufferUtils.blockLast throws ClientAbortException, SocketTimeoutException, IOException but as RuntimeException. Not ideal but works
                Thread.sleep(2000);
                maxRetries--;
                logger.info(String.format("Was not able to zip %s. Remaining retries: %d", container.getUrl(), maxRetries), exc);
                if (maxRetries == 0) {
                    throw exc;
                }
            }

        }
        return isSuccess;
    }

    abstract protected Flux<DataBuffer> getDataBufferFlux(URI encodedUrl, Optional<String> token) throws URISyntaxException, MalformedURLException;

    protected URI getEncodedURI(String decodedUrl) {
        return UriComponentsBuilder.fromHttpUrl(decodedUrl).build().encode(StandardCharsets.UTF_8).toUri();
    }

}
