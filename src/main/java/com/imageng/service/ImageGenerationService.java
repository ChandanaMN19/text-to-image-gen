package com.imageng.service;

import com.imageng.model.GenerateRequest;
import com.imageng.model.GenerateResponse;
import com.imageng.model.GeneratedImageItem;

import java.util.List;

public interface ImageGenerationService {

    /**
     * Generates an image based on the given prompt, style, and aspect ratio.
     */
    GenerateResponse generate(GenerateRequest request);

    /**
     * Gets the generation history (most recent first).
     */
    List<GeneratedImageItem> getHistory();

    /**
     * Clears generation history.
     */
    void clearHistory();

    /**
     * Downloads raw image bytes from an image URL for direct downloading.
     */
    byte[] downloadImageBytes(String imageUrl);
}
