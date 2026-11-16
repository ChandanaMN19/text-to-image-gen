package com.imageng.controller;

import com.imageng.model.ArtStyle;
import com.imageng.model.AspectRatio;
import com.imageng.model.GenerateRequest;
import com.imageng.model.GenerateResponse;
import com.imageng.model.GeneratedImageItem;
import com.imageng.service.ImageGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ImageApiController {

    private final ImageGenerationService imageService;

    private static final List<String> EXAMPLE_PROMPTS = Arrays.asList(
            "A majestic phoenix rising from cosmic flames in space",
            "Cyberpunk cityscape at night with neon reflections",
            "Ancient magical library with floating books and crystals",
            "Underwater kingdom with bioluminescent creatures",
            "Steampunk airship floating above Victorian clouds"
    );

    public ImageApiController(ImageGenerationService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generateImage(@Valid @RequestBody GenerateRequest request) {
        GenerateResponse response = imageService.generate(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<GeneratedImageItem>> getHistory() {
        return ResponseEntity.ok(imageService.getHistory());
    }

    @DeleteMapping("/history")
    public ResponseEntity<Map<String, String>> clearHistory() {
        imageService.clearHistory();
        return ResponseEntity.ok(Map.of("message", "History cleared"));
    }

    @GetMapping("/styles")
    public ResponseEntity<List<Map<String, String>>> getStyles() {
        List<Map<String, String>> styles = ArtStyle.getAll().stream()
                .map(s -> Map.of(
                        "name", s.getDisplayName(),
                        "value", s.getPromptModifier()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(styles);
    }

    @GetMapping("/aspect-ratios")
    public ResponseEntity<List<Map<String, Object>>> getAspectRatios() {
        List<Map<String, Object>> ratios = AspectRatio.getAll().stream()
                .map(r -> Map.of(
                        "name", (Object) r.getName(),
                        "value", r.getRatio(),
                        "width", r.getWidth(),
                        "height", r.getHeight()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ratios);
    }

    @GetMapping("/examples")
    public ResponseEntity<List<String>> getExamplePrompts() {
        return ResponseEntity.ok(EXAMPLE_PROMPTS);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadImage(@RequestParam("url") String imageUrl) {
        byte[] imageBytes = imageService.downloadImageBytes(imageUrl);
        if (imageBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "ai-generated-" + System.currentTimeMillis() + ".png");

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "app", "Text-to-Image Generator (Java Spring Boot)",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
