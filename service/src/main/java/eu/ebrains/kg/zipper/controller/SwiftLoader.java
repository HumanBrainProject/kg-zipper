/*
 * Copyright 2021 EPFL/Human Brain Project PCO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.ebrains.kg.zipper.controller;

import eu.ebrains.kg.zipper.models.Container;
import eu.ebrains.kg.zipper.models.SwiftContainer;
import eu.ebrains.kg.zipper.models.FileReference;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SwiftLoader extends Loader{

    private final WebClient webClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SwiftLoader(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public List<FileReference> getUrls(Container swiftContainer){
        logger.debug(String.format("Loading urls from Swift with url %s", swiftContainer.getFileIndexUri()));
        final FileEntry.ListOfFileEntries urls = webClient.get().uri(UriUtils.decode(swiftContainer.getFileIndexUri(), StandardCharsets.UTF_8)).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(FileEntry.ListOfFileEntries.class).block();
        return CollectionUtils.isEmpty(urls) ? Collections.emptyList() : urls.stream().map(u -> {
            boolean ignore = u.getName() == null || u.getName().startsWith(".") || u.getName().contains("/.") || (u.getBytes() !=null && u.getBytes() == 0) || (u.getContentType()!=null && u.getContentType().equals("application/directory"));
            return new FileReference(String.format("%s/%s", swiftContainer.getRootPath(false), u.getName().replace("?", "%3F")), ignore);
        }).collect(Collectors.toList());
    }

    @Override
    protected Flux<DataBuffer> getDataBufferFlux(URI encodedUrl, Optional<String> token) {
        return Loader.withAuth(webClient.get().uri(encodedUrl), token).retrieve().bodyToFlux(DataBuffer .class);
    }

}
