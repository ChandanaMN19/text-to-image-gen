export default function handler(req, res) {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.status(200).json([
        { name: "Photorealistic", value: "photorealistic, highly detailed, 8k" },
        { name: "Anime", value: "anime style, vibrant colors, manga inspired" },
        { name: "Digital Art", value: "digital art, concept art, trending on artstation" },
        { name: "Oil Painting", value: "oil painting, classical art, fine art" },
        { name: "Watercolor", value: "watercolor painting, soft colors, artistic" },
        { name: "Sketch", value: "pencil sketch, hand drawn, artistic sketch, line art" },
        { name: "Cyberpunk", value: "cyberpunk, neon lights, futuristic, sci-fi" },
        { name: "Fantasy", value: "fantasy art, magical, ethereal, mystical" },
        { name: "Minimalist", value: "minimalist, clean, simple, modern" }
    ]);
}
