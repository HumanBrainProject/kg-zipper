package eu.ebrains.kg.zipper.controller;

import eu.ebrains.kg.zipper.models.Container;
import eu.ebrains.kg.zipper.models.DataProxyContainer;
import eu.ebrains.kg.zipper.models.FileReference;
import eu.ebrains.kg.zipper.models.dataProxy.DataProxyFiles;
import eu.ebrains.kg.zipper.models.swift.FileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DataProxyLoader extends Loader {
    private final WebClient webClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DataProxyLoader(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public List<FileReference> getUrls(Container dataProxyContainer) {
        logger.info(String.format("Loading urls from dataProxy with url %s", dataProxyContainer.getFileIndexUri()));
        final DataProxyFiles dataProxyFiles = webClient
                .get()
                .uri(UriUtils.decode(dataProxyContainer.getFileIndexUri(), StandardCharsets.UTF_8))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> {
                    if (dataProxyContainer.getBearerToken().isPresent()) {
                        httpHeaders.set("Authorization", dataProxyContainer.getBearerToken().get());
                    }
                })
                .retrieve()
                .bodyToMono(DataProxyFiles.class)
                .block();
        if (dataProxyFiles != null && dataProxyFiles.getObjects() != null) {
            List<FileEntry> urls = dataProxyFiles.getObjects().stream().filter(x -> x.getBytes() > 0).collect(Collectors.toList());
            return CollectionUtils.isEmpty(urls) ? Collections.emptyList() : urls.stream().map(u -> new FileReference(String.format("%s/%s?storage=%s", dataProxyContainer.getRootPath(false), u.getName(), u.getStorage()), u.getName())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    @Override
    protected Flux<DataBuffer> getDataBufferFlux(URI encodedUrl, Optional<String> token) throws URISyntaxException, MalformedURLException {
        Map response = Loader.withAuth(webClient.get().uri(new URI(String.format("%s&redirect=false", encodedUrl))), token).retrieve().bodyToMono(Map.class).block();
        URI url = getEncodedURI((String) response.get("url"));
        return webClient.get().uri(url).retrieve().bodyToFlux(DataBuffer.class);
    }
}
