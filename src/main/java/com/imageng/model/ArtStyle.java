package com.imageng.model;

import java.util.Arrays;
import java.util.List;

public enum ArtStyle {
    PHOTOREALISTIC("Photorealistic", "photorealistic, highly detailed, 8k"),
    ANIME("Anime", "anime style, vibrant colors, manga inspired"),
    DIGITAL_ART("Digital Art", "digital art, concept art, trending on artstation"),
    OIL_PAINTING("Oil Painting", "oil painting, classical art, fine art"),
    WATERCOLOR("Watercolor", "watercolor painting, soft colors, artistic"),
    SKETCH("Sketch", "pencil sketch, hand drawn, artistic sketch, line art"),
    CYBERPUNK("Cyberpunk", "cyberpunk, neon lights, futuristic, sci-fi"),
    FANTASY("Fantasy", "fantasy art, magical, ethereal, mystical"),
    MINIMALIST("Minimalist", "minimalist, clean, simple, modern");

    private final String displayName;
    private final String promptModifier;

    ArtStyle(String displayName, String promptModifier) {
        this.displayName = displayName;
        this.promptModifier = promptModifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPromptModifier() {
        return promptModifier;
    }

    public static ArtStyle fromString(String styleName) {
        if (styleName == null || styleName.isBlank()) {
            return PHOTOREALISTIC;
        }
        for (ArtStyle style : values()) {
            if (style.name().equalsIgnoreCase(styleName.replace(" ", "_")) 
                    || style.displayName.equalsIgnoreCase(styleName)) {
                return style;
            }
        }
        return PHOTOREALISTIC;
    }

    public static List<ArtStyle> getAll() {
        return Arrays.asList(values());
    }
}
