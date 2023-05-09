package eu.ebrains.kg.zipper.models;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface Container {
    boolean nextPage(List<FileReference> urls);
    String getFileIndexUri();
    String getUrl();
    String getRootPath(boolean withPrefix);
    Optional<String> getBearerToken();

    static String getEncodedUrl(String url) {
        if(!url.contains("?")) {
            return url;
        }
        String[] splitByQueryParams = url.split("\\?");
        String[] splittedUrl = splitByQueryParams[1].split("&");
        Map<String, String> queryParams = new HashMap<>();
        for (String s : splittedUrl) {
            String[] splittedQueryParams = s.split("=");
            queryParams.put(splittedQueryParams[0], UriUtils.encode(splittedQueryParams[1], StandardCharsets.UTF_8));
        }
        String joinedParams = queryParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        return String.format("%s?%s", splitByQueryParams[0], joinedParams);
    }

    default String getRelativePath(boolean withPrefix){
        final String root = getRootPath(false);
        final String[] rootSplit = root.split("/");
        String lastPathElement = rootSplit[rootSplit.length-1];
        if(withPrefix){
            return getRootPath(true).substring(root.length()-lastPathElement.length());
        }
        else{
            return lastPathElement;
        }
    }

    default String getTargetZipName(){
        return String.format("%s.zip", StringUtils.strip(getRelativePath(true), "/").replaceAll("/", "-"));
    }
}
