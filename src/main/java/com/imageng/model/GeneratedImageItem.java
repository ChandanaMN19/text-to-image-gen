package com.imageng.model;

public class GeneratedImageItem {
    private String id;
    private String url;
    private String prompt;
    private String style;
    private String aspectRatio;
    private String engine;
    private long timestamp;

    public GeneratedImageItem() {
    }

    public GeneratedImageItem(String id, String url, String prompt, String style, String aspectRatio, String engine,
            long timestamp) {
        this.id = id;
        this.url = url;
        this.prompt = prompt;
        this.style = style;
        this.aspectRatio = aspectRatio;
        this.engine = engine;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
