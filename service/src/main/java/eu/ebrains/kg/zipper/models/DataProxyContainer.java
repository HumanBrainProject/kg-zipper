package eu.ebrains.kg.zipper.models;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class DataProxyContainer implements Container {
    private final URI uri;
    private final URI baseUri;
    private String marker;
    private Optional<String> bearerToken;

    public DataProxyContainer(String url, Optional<String> bearerToken) throws URISyntaxException {
        String encodedUrl = Container.getEncodedUrl(url);
        this.uri = new URI(encodedUrl);
        this.bearerToken = bearerToken;
        this.baseUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment());
    }

    public String getUrl() {
        return this.uri.toString();
    }

    public Optional<String> getBearerToken() {
        return this.bearerToken;
    }

    public String getRootPath(boolean withPrefix) {
        if (withPrefix) {
            final List<NameValuePair> params = URLEncodedUtils.parse(this.uri, StandardCharsets.UTF_8.name());
            final Optional<String> prefix = params.stream().filter(p -> "prefix".equals(p.getName())).findFirst().map(NameValuePair::getValue);
            if (prefix.isPresent()) {
                return String.format("%s/%s", this.baseUri.toString(), prefix.get());
            }
        }
        return this.baseUri.toString();
    }

    public boolean nextPage(List<FileReference> urls) {
        if (urls == null || urls.isEmpty()) {
            return false;
        }
        FileReference fileReference = urls.get(urls.size() - 1);
        this.marker = fileReference.getName();
        return true;
    }


    public String getFileIndexUri() {
        String markerQueryString = this.uri.toString().contains("prefix") ? String.format("&marker=%s", marker) : String.format("?marker=%s", marker);
        return this.marker == null ? this.uri.toString() : String.format("%s%s", this.uri, markerQueryString, StandardCharsets.UTF_8);
    }
}
