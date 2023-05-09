package eu.ebrains.kg.zipper.models.dataProxy;

import eu.ebrains.kg.zipper.models.swift.FileEntry;

public class DataProxyFiles {
    private FileEntry.ListOfFileEntries objects;
    private String container;
    private String prefix;
    private String delimiter;
    private String marker;
    private Integer limit;

    public FileEntry.ListOfFileEntries getObjects() {
        return objects;
    }

    public void setObjects(FileEntry.ListOfFileEntries objects) {
        this.objects = objects;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
