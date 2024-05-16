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

package eu.ebrains.kg.zipper.api;

import eu.ebrains.kg.zipper.controller.DataProxyLoader;
import eu.ebrains.kg.zipper.controller.Loader;
import eu.ebrains.kg.zipper.controller.SwiftLoader;
import eu.ebrains.kg.zipper.models.Container;
import eu.ebrains.kg.zipper.models.DataProxyContainer;
import eu.ebrains.kg.zipper.models.FileReference;
import eu.ebrains.kg.zipper.models.SwiftContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

@RestController
public class ZipperAPI {
    private final SwiftLoader swiftLoader;
    private final DataProxyLoader dataProxyLoader;

    public ZipperAPI(SwiftLoader swiftLoader, DataProxyLoader dataProxyLoader) {
        this.swiftLoader = swiftLoader;
        this.dataProxyLoader = dataProxyLoader;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @CrossOrigin
    @PostMapping(value = "/zip", produces = "application/zip")
    public void zip(@RequestParam("container") String containerUrl, @RequestParam MultiValueMap<String, String> form, HttpServletResponse response) {
        Optional<String> bearerToken = Optional.ofNullable(form.getFirst("token"));
        streamZip(containerUrl, bearerToken, response);
    }

    @CrossOrigin
    @GetMapping(value = "/zip", produces = "application/zip")
    public void zipContainer(@RequestParam("container") String containerUrl, @RequestHeader("Authorization") Optional<String> bearerToken, HttpServletResponse response) {
        streamZip(containerUrl, bearerToken, response);
    }

    private void streamZip(String containerUrl, Optional<String> bearerToken, HttpServletResponse response) {
        try {
            String decodeContainerUrl = URLDecoder.decode(containerUrl, StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("^https:\\/\\/data-proxy(-dev|-int|-ppd)?\\.ebrains\\.eu", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(decodeContainerUrl);
            boolean matchFound = matcher.find();
            if (matchFound || decodeContainerUrl.startsWith("http://localhost:3000")) {
                DataProxyContainer container = new DataProxyContainer(decodeContainerUrl, bearerToken);
                tryToZip(response, container, this.dataProxyLoader);
            } else if (decodeContainerUrl.startsWith("https://object.cscs")) {
                SwiftContainer container = new SwiftContainer(decodeContainerUrl);
                tryToZip(response, container, this.swiftLoader);
            } else {
                throw new IllegalArgumentException("This service currently only supports targets of https://object.cscs.ch/* or https://data-proxy.ebrains.eu/*");
            }
        } catch (URISyntaxException e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            logger.error(String.format("Container url is not valid: %s", containerUrl), e);
        }
    }

    private void tryToZip(HttpServletResponse response, Container container, Loader loader) {
        logger.info(String.format("Zipping %s", container.getUrl()));
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", container.getTargetZipName()));
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            zipOutputStream.setLevel(0);
            List<FileReference> urls = loader.getUrls(container);
            while (!CollectionUtils.isEmpty(urls)) {
                for (FileReference url : urls) {
                    loader.download(url, container, zipOutputStream, logger);
                }
                container.nextPage(urls);
                urls = loader.getUrls(container);
            }
        } catch (Exception e) {
            logger.error(String.format("Was not able to zip %s", container.getUrl()), e);
            throw new RuntimeException(String.format("Was not able to zip %s", container.getUrl()), e);
        }
    }

}
