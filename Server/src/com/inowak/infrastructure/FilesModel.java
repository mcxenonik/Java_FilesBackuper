package com.inowak.infrastructure;

public class FilesModel {

    public String relativePath;
    public String absolutePath;
    public long crcHash;

    public FilesModel(String fullPath, String relPath, long hash) {

        relativePath = relPath;
        absolutePath = fullPath;
        crcHash = hash;
    }
}
