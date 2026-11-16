export default async function handler(req, res) {
    // Enable CORS
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    if (req.method !== 'POST') {
        return res.status(405).json({ success: false, error: 'Method not allowed' });
    }

    try {
        const { prompt, style = 'Photorealistic', aspectRatio = '1:1', provider = 'auto', apiKey } = req.body || {};

        if (!prompt || !prompt.trim()) {
            return res.status(400).json({ success: false, error: 'Prompt is required' });
        }

        const STYLE_MODIFIERS = {
            'Photorealistic': 'photorealistic, highly detailed, 8k',
            'Anime': 'anime style, vibrant colors, manga inspired',
            'Digital Art': 'digital art, concept art, trending on artstation',
            'Oil Painting': 'oil painting, classical art, fine art',
            'Watercolor': 'watercolor painting, soft colors, artistic',
            'Sketch': 'pencil sketch, hand drawn, artistic sketch, line art',
            'Cyberpunk': 'cyberpunk, neon lights, futuristic, sci-fi',
            'Fantasy': 'fantasy art, magical, ethereal, mystical',
            'Minimalist': 'minimalist, clean, simple, modern'
        };

        const modifier = STYLE_MODIFIERS[style] || 'highly detailed';
        const fullPrompt = `${prompt.trim()}, ${modifier}`;

        const RATIO_MAP = {
            '1:1': { width: 1024, height: 1024, hordeW: 512, hordeH: 512 },
            '16:9': { width: 1024, height: 576, hordeW: 512, hordeH: 288 },
            '9:16': { width: 576, height: 1024, hordeW: 288, hordeH: 512 },
            '21:9': { width: 1024, height: 439, hordeW: 512, hordeH: 256 }
        };

        const dims = RATIO_MAP[aspectRatio] || RATIO_MAP['1:1'];
        let imageUrl = null;
        let engine = 'Pollinations AI (Flux)';

        // 1. Check custom OpenAI API Key
        const activeOpenAiKey = apiKey || process.env.OPENAI_API_KEY;
        if (provider === 'openai' || (provider === 'auto' && activeOpenAiKey)) {
            try {
                const openAiRes = await fetch('https://api.openai.com/v1/images/generations', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${activeOpenAiKey}`
                    },
                    body: JSON.stringify({
                        model: 'dall-e-3',
                        prompt: fullPrompt,
                        n: 1,
                        size: '1024x1024'
                    })
                });
                const openAiData = await openAiRes.json();
                if (openAiData.data?.[0]?.url) {
                    imageUrl = openAiData.data[0].url;
                    engine = 'OpenAI DALL-E 3';
                }
            } catch (err) {
                console.error('OpenAI failed:', err);
            }
        }

        // 2. Direct Pollinations or AI Horde fallback
        if (!imageUrl) {
            const seed = Math.floor(Math.random() * 1000000000);
            const encodedPrompt = encodeURIComponent(fullPrompt);
            imageUrl = `https://image.pollinations.ai/prompt/${encodedPrompt}?width=${dims.width}&height=${dims.height}&seed=${seed}&nologo=true&enhance=true`;
            engine = 'Pollinations AI (Flux)';
        }

        return res.status(200).json({
            success: true,
            imageUrl: imageUrl,
            prompt: prompt,
            style: style,
            aspectRatio: aspectRatio,
            engine: engine,
            timestamp: Date.now()
        });

    } catch (err) {
        console.error('Vercel API error:', err);
        return res.status(500).json({ success: false, error: err.message || 'Server error' });
    }
}
