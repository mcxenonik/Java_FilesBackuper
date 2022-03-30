package com.company.infrastructure;

public class FilesModel {

    public String relativePath;
    public String absolutePath;
    public long crcHash;

    public FilesModel(String fullPath, String relPath, long hash)
    {
        this.relativePath = relPath;
        this.absolutePath = fullPath;
        this.crcHash = hash;
    }

}
