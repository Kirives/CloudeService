package com.example.demo.dto;

import lombok.Builder;

@Builder
public class ObjectDto {
    private String name;
    private String path;
    private boolean isDirectory;
    private String encodePath;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getEncodePath() {
        return encodePath;
    }

    public void setEncodePath(String encodePath) {
        this.encodePath = encodePath;
    }
}
