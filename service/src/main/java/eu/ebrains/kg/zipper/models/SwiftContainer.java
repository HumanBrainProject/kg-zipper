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

package eu.ebrains.kg.zipper.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class SwiftContainer implements Container {
    private final URI uri;
    private final URI baseUri;
    private String marker;

    public SwiftContainer(String url) throws URISyntaxException {
        String encodedUrl = Container.getEncodedUrl(url);
        this.uri = new URI(encodedUrl);
        this.baseUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());
    }

    @Override
    public String toString() {
        return "SwiftContainer{" +
                "url='" + uri + '\'' +
                '}';
    }

    public Optional<String> getBearerToken() {
        return Optional.empty();
    }

    public String getUrl() {
        return this.uri.toString();
    }

    public String getRootPath(boolean withPrefix) {
        if(withPrefix) {
            final List<NameValuePair> params = URLEncodedUtils.parse(this.uri, StandardCharsets.UTF_8.name());
            final Optional<String> prefix = params.stream().filter(p -> "prefix".equals(p.getName())).findFirst().map(NameValuePair::getValue);
            if (prefix.isPresent()) {
                return String.format("%s/%s", this.baseUri.toString(), prefix.get());
            }
        }
        return this.baseUri.toString();
    }

    public boolean nextPage(List<FileReference> urls){
        if(urls == null || urls.isEmpty()){
            return false;
        }
        this.marker = urls.get(urls.size()-1).getRelativeObjectPath(this);
        return true;
    }


    public String getFileIndexUri(){
        String extended = String.format("%s%s%s", this.uri.toString(), this.uri.toString().contains("?") ? "&" : "?", "format=json");
        return this.marker == null ? this.uri.toString() : String.format("%s&marker=%s", extended, UriUtils.encode(this.marker, StandardCharsets.UTF_8));
    }

}

