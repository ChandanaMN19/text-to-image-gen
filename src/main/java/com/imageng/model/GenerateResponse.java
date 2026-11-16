package com.imageng.model;

public class GenerateResponse {
    private boolean success;
    private String imageUrl;
    private String prompt;
    private String style;
    private String aspectRatio;
    private String engine;
    private long timestamp;
    private String error;

    public GenerateResponse() {
    }


    public static GenerateResponse ok(String imageUrl, String prompt, String style, String aspectRatio, String engine) {
        GenerateResponse res = new GenerateResponse();
        res.setSuccess(true);
        res.setImageUrl(imageUrl);
        res.setPrompt(prompt);
        res.setStyle(style);
        res.setAspectRatio(aspectRatio);
        res.setEngine(engine);
        res.setTimestamp(System.currentTimeMillis());
        return res;
    }

    public static GenerateResponse fail(String error) {
        GenerateResponse res = new GenerateResponse();
        res.setSuccess(false);
        res.setError(error);
        res.setTimestamp(System.currentTimeMillis());
        return res;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
