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

public class FileReference {
    private final String url;
    private boolean ignore;
    private String name;

    public FileReference(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public FileReference(String url, boolean ignore) {
        this.url = url;
        this.ignore = ignore;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getRelativeFilePath(Container container) {
        final String rootPath = container.getRootPath(true);
        final String relativePath = url.replace(rootPath, "");
        return relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
    }

    public String getRelativeObjectPath(Container container) {
        final String rootPath = container.getRootPath(false);
        final String relativePath = url.replace(rootPath, "");
        return relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
    }

}
