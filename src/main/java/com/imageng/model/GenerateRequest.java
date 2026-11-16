package com.imageng.model;

import jakarta.validation.constraints.NotBlank;

public class GenerateRequest {

    @NotBlank(message = "Prompt is required and cannot be empty")
    private String prompt;

    private String style = "Photorealistic";

    private String aspectRatio = "1:1";

    private String provider = "auto";

    private String apiKey;

    public GenerateRequest() {
    }

    public GenerateRequest(String prompt, String style, String aspectRatio) {
        this.prompt = prompt;
        this.style = style;
        this.aspectRatio = aspectRatio;
        this.provider = "auto";
    }

    public GenerateRequest(String prompt, String style, String aspectRatio, String provider, String apiKey) {
        this.prompt = prompt;
        this.style = style;
        this.aspectRatio = aspectRatio;
        this.provider = provider != null ? provider : "auto";
        this.apiKey = apiKey;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
