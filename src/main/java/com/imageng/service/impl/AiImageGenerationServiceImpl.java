package com.imageng.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imageng.entity.GeneratedImage;
import com.imageng.model.ArtStyle;
import com.imageng.model.AspectRatio;
import com.imageng.model.GenerateRequest;
import com.imageng.model.GenerateResponse;
import com.imageng.model.GeneratedImageItem;
import com.imageng.repository.GeneratedImageRepository;
import com.imageng.service.ImageGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AiImageGenerationServiceImpl implements ImageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AiImageGenerationServiceImpl.class);

    @Value("${LOVABLE_API_KEY:#{null}}")
    private String lovableApiKey;

    @Value("${OPENAI_API_KEY:#{null}}")
    private String openAiApiKey;

    @Autowired(required = false)
    private GeneratedImageRepository repository;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final List<GeneratedImageItem> history = new CopyOnWriteArrayList<>();
    private final AtomicLong lastPollinationsTime = new AtomicLong(0);

    public AiImageGenerationServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public AiImageGenerationServiceImpl(GeneratedImageRepository repository) {
        this();
        this.repository = repository;
    }

    @Override
    public GenerateResponse generate(GenerateRequest request) {
        String prompt = request.getPrompt() != null ? request.getPrompt().trim() : "";
        if (prompt.isEmpty()) {
            return GenerateResponse.fail("Prompt cannot be empty");
        }

        ArtStyle artStyle = ArtStyle.fromString(request.getStyle());
        AspectRatio ratio = AspectRatio.fromString(request.getAspectRatio());
        String provider = request.getProvider() != null ? request.getProvider().toLowerCase().trim() : "auto";

        String fullPrompt = prompt + ", " + artStyle.getPromptModifier();
        log.info("Generating image with prompt: '{}' [Style: {}, Ratio: {}, Provider: {}]",
                fullPrompt, artStyle.getDisplayName(), ratio.getRatio(), provider);

        try {
            String imageUrl = null;
            String engineUsed = "AI Engine";

            // 1. Explicit or Configured Lovable / Gemini Gateway
            String activeLovableKey = request.getApiKey() != null && !request.getApiKey().isBlank()
                    ? request.getApiKey()
                    : (lovableApiKey != null && !lovableApiKey.isBlank() ? lovableApiKey
                            : System.getenv("LOVABLE_API_KEY"));

            if ("lovable".equals(provider)
                    || ("auto".equals(provider) && activeLovableKey != null && !activeLovableKey.isBlank())) {
                if (activeLovableKey != null && !activeLovableKey.isBlank()) {
                    try {
                        imageUrl = generateWithLovable(fullPrompt, activeLovableKey);
                        engineUsed = "Lovable / Gemini Gateway";
                    } catch (Exception e) {
                        log.warn("Lovable gateway failed: {}", e.getMessage());
                        if ("lovable".equals(provider))
                            throw e;
                    }
                }
            }

            // 2. Explicit or Configured OpenAI DALL-E
            String activeOpenAiKey = request.getApiKey() != null && !request.getApiKey().isBlank()
                    ? request.getApiKey()
                    : (openAiApiKey != null && !openAiApiKey.isBlank() ? openAiApiKey
                            : System.getenv("OPENAI_API_KEY"));

            if (imageUrl == null && ("openai".equals(provider)
                    || ("auto".equals(provider) && activeOpenAiKey != null && !activeOpenAiKey.isBlank()))) {
                if (activeOpenAiKey != null && !activeOpenAiKey.isBlank()) {
                    try {
                        imageUrl = generateWithOpenAi(fullPrompt, activeOpenAiKey, ratio);
                        engineUsed = "OpenAI DALL-E 3";
                    } catch (Exception e) {
                        log.warn("OpenAI API failed: {}", e.getMessage());
                        if ("openai".equals(provider))
                            throw e;
                    }
                }
            }

            // 3. Explicit Horde selection
            if (imageUrl == null && "horde".equals(provider)) {
                imageUrl = generateWithHorde(fullPrompt, ratio);
                engineUsed = "AI Horde (Stable Diffusion)";
            }

            // 4. Auto mode: try Pollinations IF not in cooldown; otherwise seamlessly use
            // AI Horde!
            if (imageUrl == null && ("auto".equals(provider) || "pollinations".equals(provider))) {
                long elapsed = (System.currentTimeMillis() - lastPollinationsTime.get()) / 1000;

                if (elapsed >= 46 || lastPollinationsTime.get() == 0) {
                    imageUrl = generateWithPollinations(fullPrompt, ratio);
                    if (imageUrl != null) {
                        engineUsed = "Pollinations AI (Flux)";
                    }
                } else {
                    log.info(
                            "Pollinations cooldown active ({}s / 45s). Seamlessly routing to AI Horde to prevent rate-limit error banner.",
                            elapsed);
                }

                // If Pollinations was skipped or returned null, route to AI Horde
                if (imageUrl == null) {
                    imageUrl = generateWithHorde(fullPrompt, ratio);
                    if (imageUrl != null) {
                        engineUsed = "AI Horde (Stable Diffusion)";
                    }
                }

                // If Horde didn't complete and user specifically selected Pollinations, give
                // exact cooldown time
                if (imageUrl == null && "pollinations".equals(provider)) {
                    long remaining = 45 - elapsed;
                    if (remaining > 0) {
                        return GenerateResponse.fail("Pollinations rate limit cooldown: please wait " + remaining
                                + " seconds before next generation.");
                    }
                }
            }

            // Final fallback to direct generation
            if (imageUrl == null) {
                // Return Pollinations as last resort
                imageUrl = generateWithPollinationsDirect(fullPrompt, ratio);
                engineUsed = "Pollinations AI";
            }

            // Record in Database and Memory History
            long timestamp = System.currentTimeMillis();
            String recordId = UUID.randomUUID().toString();

            if (repository != null) {
                try {
                    GeneratedImage entity = new GeneratedImage(
                            prompt,
                            imageUrl,
                            artStyle.getDisplayName(),
                            ratio.getRatio(),
                            engineUsed,
                            timestamp);
                    GeneratedImage saved = repository.save(entity);
                    recordId = String.valueOf(saved.getId());
                    log.info("Persisted generated image to database (ID: {})", recordId);
                } catch (Exception ex) {
                    log.warn("Failed to persist to database: {}", ex.getMessage());
                }
            }

            GeneratedImageItem historyItem = new GeneratedImageItem(
                    recordId,
                    imageUrl,
                    prompt,
                    artStyle.getDisplayName(),
                    ratio.getRatio(),
                    engineUsed,
                    timestamp);

            history.add(0, historyItem);
            if (history.size() > 24) {
                history.remove(history.size() - 1);
            }

            return GenerateResponse.ok(imageUrl, prompt, artStyle.getDisplayName(), ratio.getRatio(), engineUsed);

        } catch (Exception e) {
            log.error("Error during image generation: ", e);
            return GenerateResponse.fail("Failed to generate image: " + e.getMessage());
        }
    }

    /**
     * Pollinations AI Generator with cooldown guard.
     */
    private String generateWithPollinations(String fullPrompt, AspectRatio ratio) {
        try {
            long seed = (long) (Math.random() * 1_000_000_000L);
            String encodedPrompt = URLEncoder.encode(fullPrompt, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://image.pollinations.ai/prompt/%s?width=%d&height=%d&seed=%d&nologo=true&enhance=true",
                    encodedPrompt, ratio.getWidth(), ratio.getHeight(), seed);

            HttpRequest testReq = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(12))
                    .build();

            HttpResponse<Void> testRes = httpClient.send(testReq, HttpResponse.BodyHandlers.discarding());
            if (testRes.statusCode() == 200) {
                lastPollinationsTime.set(System.currentTimeMillis());
                return url;
            } else {
                log.warn("Pollinations returned status {}. Bypassing to fallback.", testRes.statusCode());
                return null;
            }
        } catch (Exception e) {
            log.warn("Pollinations check failed: {}", e.getMessage());
            return null;
        }
    }

    private String generateWithPollinationsDirect(String fullPrompt, AspectRatio ratio) {
        long seed = (long) (Math.random() * 1_000_000_000L);
        String encodedPrompt = URLEncoder.encode(fullPrompt, StandardCharsets.UTF_8);
        lastPollinationsTime.set(System.currentTimeMillis());
        return String.format(
                "https://image.pollinations.ai/prompt/%s?width=%d&height=%d&seed=%d&nologo=true&enhance=true",
                encodedPrompt, ratio.getWidth(), ratio.getHeight(), seed);
    }

    /**
     * AI Horde (Stable Diffusion) - Free community grid with NO 45-second IP
     * cooldown.
     */
    private String generateWithHorde(String fullPrompt, AspectRatio ratio) {
        try {
            int width = 512;
            int height = 512;
            if (ratio == AspectRatio.LANDSCAPE) {
                width = 512;
                height = 288;
            } else if (ratio == AspectRatio.PORTRAIT) {
                width = 288;
                height = 512;
            } else if (ratio == AspectRatio.WIDE) {
                width = 512;
                height = 256;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("width", width);
            params.put("height", height);
            params.put("steps", 20);

            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("prompt", fullPrompt);
            reqBody.put("params", params);

            String json = objectMapper.writeValueAsString(reqBody);

            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://aihorde.net/api/v2/generate/async"))
                    .header("Content-Type", "application/json")
                    .header("apikey", "0000000000")
                    .header("Client-Agent", "TextToImageApp:1.0:user")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> postRes = httpClient.send(postReq, HttpResponse.BodyHandlers.ofString());
            if (postRes.statusCode() >= 400) {
                log.warn("AI Horde submission failed with code {}: {}", postRes.statusCode(), postRes.body());
                return null;
            }

            JsonNode submitNode = objectMapper.readTree(postRes.body());
            String id = submitNode.path("id").asText();
            if (id == null || id.isBlank())
                return null;

            log.info("AI Horde generation queued (ID: {}). Polling for completion...", id);

            // Poll for up to 30 seconds
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 30000) {
                Thread.sleep(2000);
                HttpRequest checkReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://aihorde.net/api/v2/generate/check/" + id))
                        .header("Client-Agent", "TextToImageApp:1.0:user")
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> checkRes = httpClient.send(checkReq, HttpResponse.BodyHandlers.ofString());
                if (checkRes.statusCode() == 200) {
                    JsonNode checkNode = objectMapper.readTree(checkRes.body());
                    if (checkNode.path("done").asBoolean(false)) {
                        // Retrieve image URL
                        HttpRequest statusReq = HttpRequest.newBuilder()
                                .uri(URI.create("https://aihorde.net/api/v2/generate/status/" + id))
                                .header("Client-Agent", "TextToImageApp:1.0:user")
                                .GET()
                                .timeout(Duration.ofSeconds(10))
                                .build();
                        HttpResponse<String> statusRes = httpClient.send(statusReq,
                                HttpResponse.BodyHandlers.ofString());
                        if (statusRes.statusCode() == 200) {
                            JsonNode statusNode = objectMapper.readTree(statusRes.body());
                            JsonNode gens = statusNode.path("generations");
                            if (gens.isArray() && gens.size() > 0) {
                                String imgUrl = gens.get(0).path("img").asText();
                                if (imgUrl != null && !imgUrl.isBlank()) {
                                    log.info("AI Horde generated image successfully!");
                                    return imgUrl;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("AI Horde generation failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Lovable AI Gateway generation (matches original Supabase edge function)
     */
    private String generateWithLovable(String fullPrompt, String apiKey) throws Exception {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", "google/gemini-2.5-flash-image-preview");
        bodyMap.put("messages", List.of(Map.of("role", "user", "content", fullPrompt)));
        bodyMap.put("modalities", List.of("image", "text"));

        String jsonBody = objectMapper.writeValueAsString(bodyMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ai.gateway.lovable.dev/v1/chat/completions"))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Lovable AI Gateway responded with code " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode imagesNode = root.path("choices").path(0).path("message").path("images");
        if (imagesNode.isArray() && imagesNode.size() > 0) {
            return imagesNode.get(0).path("image_url").path("url").asText();
        }
        throw new RuntimeException("No image found in Lovable AI Gateway response");
    }

    /**
     * OpenAI DALL-E 3 generation
     */
    private String generateWithOpenAi(String fullPrompt, String apiKey, AspectRatio ratio) throws Exception {
        String size = "1024x1024";
        if (ratio == AspectRatio.LANDSCAPE) {
            size = "1792x1024";
        } else if (ratio == AspectRatio.PORTRAIT) {
            size = "1024x1792";
        }

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", "dall-e-3");
        bodyMap.put("prompt", fullPrompt);
        bodyMap.put("n", 1);
        bodyMap.put("size", size);

        String jsonBody = objectMapper.writeValueAsString(bodyMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/images/generations"))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("OpenAI returned status " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("data").path(0).path("url").asText();
    }

    @Override
    public List<GeneratedImageItem> getHistory() {
        if (repository != null) {
            try {
                return repository.findTop24ByOrderByTimestampDesc().stream()
                        .map(img -> new GeneratedImageItem(
                                String.valueOf(img.getId()),
                                img.getImageUrl(),
                                img.getPrompt(),
                                img.getStyle(),
                                img.getAspectRatio(),
                                img.getEngine(),
                                img.getTimestamp()))
                        .collect(Collectors.toList());
            } catch (Exception ex) {
                log.warn("Failed to read history from database: {}", ex.getMessage());
            }
        }
        return Collections.unmodifiableList(history);
    }

    @Override
    public void clearHistory() {
        if (repository != null) {
            try {
                repository.deleteAll();
            } catch (Exception ex) {
                log.warn("Failed to delete history from database: {}", ex.getMessage());
            }
        }
        history.clear();
    }

    @Override
    public byte[] downloadImageBytes(String imageUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            log.error("Failed to download image bytes from {}: {}", imageUrl, e.getMessage());
        }
        return new byte[0];
    }
}
