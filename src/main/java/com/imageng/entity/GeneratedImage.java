package com.imageng.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "generated_images")
public class GeneratedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000, nullable = false)
    private String prompt;

    @Column(length = 4000, nullable = false)
    private String imageUrl;

    @Column(name = "art_style")
    private String style;

    @Column(name = "aspect_ratio")
    private String aspectRatio;

    @Column(name = "ai_engine")
    private String engine;

    @Column(nullable = false)
    private Long timestamp;

    public GeneratedImage() {
    }

    public GeneratedImage(String prompt, String imageUrl, String style, String aspectRatio, String engine, Long timestamp) {
        this.prompt = prompt;
        this.imageUrl = imageUrl;
        this.style = style;
        this.aspectRatio = aspectRatio;
        this.engine = engine;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
