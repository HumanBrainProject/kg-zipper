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
        int maxRetries = 1;
        boolean isSuccess = false;

        String filename = removeQueryParameters(fileReference.getRelativeFilePath(container));

        zipOutputStream.putNextEntry(new ZipEntry(filename));
        URI encodedUrl = getEncodedURI(fileReference.getUrl());

        while (maxRetries > 0 && !isSuccess) {
            try {
                final Flux<DataBuffer> dataBufferFlux = getDataBufferFlux(encodedUrl, container.getBearerToken());
                DataBufferUtils.write(dataBufferFlux, zipOutputStream).map(DataBufferUtils::release).blockLast();
                logger.info("Downloaded {}", fileReference.getUrl());
                isSuccess = true;
            } catch (RuntimeException exc) { // DataBufferUtils.blockLast throws ClientAbortException, SocketTimeoutException, IOException but as RuntimeException. Not ideal but it works.
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

    public static String removeQueryParameters(String url) {
        int questionMarkIndex = url.indexOf("?");
        return questionMarkIndex == -1 ? url : url.substring(0, questionMarkIndex);
    }

    abstract protected Flux<DataBuffer> getDataBufferFlux(URI encodedUrl, Optional<String> token) throws URISyntaxException, MalformedURLException;

    protected URI getEncodedURI(String url) {
        // Essayer de décoder l'URL
        String decodedUrl;
        try {
            decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            // Si le décodage échoue, considérer que l'URL n'était pas encodée
            decodedUrl = url;
        }

        // Si l'URL décodée est la même que l'originale, cela signifie qu'elle n'était pas encodée
        if (decodedUrl.equals(url)) {
            // Encoder l'URL
            return UriComponentsBuilder.fromHttpUrl(decodedUrl).build().encode(StandardCharsets.UTF_8).toUri();
        } else {
            // Si l'URL était déjà encodée, la retourner telle quelle
            return URI.create(url);
        }
    }

}
