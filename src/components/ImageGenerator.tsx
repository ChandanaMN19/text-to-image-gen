import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { Loader2, Sparkles, Download, Share2, Clock } from "lucide-react";
import { toast } from "sonner";

const EXAMPLE_PROMPTS = [
  "A majestic phoenix rising from cosmic flames in space",
  "Cyberpunk cityscape at night with neon reflections",
  "Ancient magical library with floating books and crystals",
  "Underwater kingdom with bioluminescent creatures",
  "Steampunk airship floating above Victorian clouds",
];

const ART_STYLES = [
  { name: "Photorealistic", value: "photorealistic, highly detailed, 8k" },
  { name: "Anime", value: "anime style, vibrant colors, manga inspired" },
  { name: "Digital Art", value: "digital art, concept art, trending on artstation" },
  { name: "Oil Painting", value: "oil painting, classical art, fine art" },
  { name: "Watercolor", value: "watercolor painting, soft colors, artistic" },
  { name: "Cyberpunk", value: "cyberpunk, neon lights, futuristic, sci-fi" },
  { name: "Fantasy", value: "fantasy art, magical, ethereal, mystical" },
  { name: "Minimalist", value: "minimalist, clean, simple, modern" },
];

const ASPECT_RATIOS = [
  { name: "Square", value: "1:1", width: 1024, height: 1024 },
  { name: "Landscape", value: "16:9", width: 1024, height: 576 },
  { name: "Portrait", value: "9:16", width: 576, height: 1024 },
  { name: "Wide", value: "21:9", width: 1024, height: 439 },
];

interface GeneratedImage {
  url: string;
  prompt: string;
  timestamp: number;
  style: string;
}

export default function ImageGenerator() {
  const [prompt, setPrompt] = useState("");
  const [selectedStyle, setSelectedStyle] = useState(ART_STYLES[0]);
  const [selectedRatio, setSelectedRatio] = useState(ASPECT_RATIOS[0]);
  const [isGenerating, setIsGenerating] = useState(false);
  const [currentImage, setCurrentImage] = useState<string | null>(null);
  const [history, setHistory] = useState<GeneratedImage[]>([]);

  const generateImage = async () => {
    if (!prompt.trim()) {
      toast.error("Please enter a prompt");
      return;
    }

    setIsGenerating(true);
    
    try {
      const fullPrompt = `${prompt}, ${selectedStyle.value}`;
      
      const response = await fetch(
        `${import.meta.env.VITE_SUPABASE_URL}/functions/v1/generate-image`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY}`,
          },
          body: JSON.stringify({ prompt: fullPrompt }),
        }
      );

      if (!response.ok) {
        const error = await response.json();
        if (response.status === 429) {
          toast.error("Rate limit exceeded. Please try again later.");
        } else if (response.status === 402) {
          toast.error("Payment required. Please add funds to continue.");
        } else {
          toast.error(error.error || "Failed to generate image");
        }
        setIsGenerating(false);
        return;
      }

      const data = await response.json();
      const imageUrl = data.imageUrl;
      
      setCurrentImage(imageUrl);
      const newImage: GeneratedImage = {
        url: imageUrl,
        prompt: prompt,
        timestamp: Date.now(),
        style: selectedStyle.name,
      };
      setHistory([newImage, ...history.slice(0, 11)]);
      
      setIsGenerating(false);
      toast.success("Image generated!");
    } catch (error) {
      console.error("Error:", error);
      toast.error("An error occurred while generating the image");
      setIsGenerating(false);
    }
  };

  const downloadImage = async () => {
    if (!currentImage) return;
    
    try {
      const response = await fetch(currentImage);
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `ai-generated-${Date.now()}.png`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast.success("Image downloaded!");
    } catch (error) {
      toast.error("Failed to download image");
    }
  };

  const shareImage = async () => {
    if (!currentImage) return;
    
    try {
      // Convert base64 to blob
      const response = await fetch(currentImage);
      const blob = await response.blob();
      
      if (navigator.share && navigator.canShare) {
        const file = new File([blob], `ai-generated-${Date.now()}.png`, { type: 'image/png' });
        
        if (navigator.canShare({ files: [file] })) {
          await navigator.share({
            title: "AI Generated Image",
            text: `Check out this AI-generated image: ${prompt}`,
            files: [file],
          });
          toast.success("Image shared!");
        } else {
          // Fallback to copying URL
          await navigator.clipboard.writeText(currentImage);
          toast.success("Image URL copied to clipboard!");
        }
      } else {
        await navigator.clipboard.writeText(currentImage);
        toast.success("Image URL copied to clipboard!");
      }
    } catch (error) {
      console.error("Share error:", error);
      toast.error("Failed to share image");
    }
  };

  const loadHistoryImage = (image: GeneratedImage) => {
    setCurrentImage(image.url);
    setPrompt(image.prompt);
    const style = ART_STYLES.find((s) => s.name === image.style);
    if (style) setSelectedStyle(style);
  };

  return (
    <div className="min-h-screen py-12 px-4">
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Header */}
        <div className="text-center">
          <div className="flex items-center justify-center gap-3 mb-4">
            <Sparkles className="w-10 h-10 text-primary" />
            <h1 className="text-5xl font-bold bg-gradient-to-r from-primary via-accent to-secondary bg-clip-text text-transparent">
              AI Image Generator
            </h1>
          </div>
          <p className="text-muted-foreground text-lg">
            Transform your imagination into stunning visuals with AI
          </p>
        </div>

        {/* Prompt Input */}
        <Card className="glass-card p-6">
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-primary" />
            Describe Your Vision
          </h2>
          <Textarea
            placeholder="A mystical forest with glowing mushrooms and fireflies..."
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            className="min-h-32 resize-none bg-background/50 backdrop-blur-sm border-primary/20 focus:border-primary/40"
          />
          <div className="mt-4 flex flex-wrap gap-2">
            {EXAMPLE_PROMPTS.map((example) => (
              <Button
                key={example}
                variant="glass"
                size="sm"
                onClick={() => setPrompt(example)}
                className="text-xs"
              >
                {example.slice(0, 40)}...
              </Button>
            ))}
          </div>
        </Card>

        {/* Art Style Selection */}
        <Card className="glass-card p-6">
          <h2 className="text-xl font-semibold mb-4">Art Style</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {ART_STYLES.map((style) => (
              <Button
                key={style.name}
                variant={selectedStyle.name === style.name ? "cosmic" : "outline"}
                onClick={() => setSelectedStyle(style)}
                className="w-full"
              >
                {style.name}
              </Button>
            ))}
          </div>
        </Card>

        {/* Aspect Ratio Selection */}
        <Card className="glass-card p-6">
          <h2 className="text-xl font-semibold mb-4">Aspect Ratio</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {ASPECT_RATIOS.map((ratio) => (
              <Button
                key={ratio.name}
                variant={selectedRatio.name === ratio.name ? "default" : "outline"}
                onClick={() => setSelectedRatio(ratio)}
              >
                {ratio.name} ({ratio.value})
              </Button>
            ))}
          </div>
        </Card>

        {/* Generate Button */}
        <Button
          variant="cosmic"
          size="lg"
          className="w-full animate-glow"
          onClick={generateImage}
          disabled={isGenerating}
        >
          {isGenerating ? (
            <>
              <Loader2 className="animate-spin" />
              Generating Magic...
            </>
          ) : (
            <>
              <Sparkles />
              Generate Image
            </>
          )}
        </Button>

        {/* Image Display */}
        <Card className="glass-card p-6">
          <div className="aspect-square rounded-lg bg-background/50 backdrop-blur-sm border-2 border-dashed border-primary/20 flex items-center justify-center overflow-hidden relative">
            {currentImage ? (
              <>
                <img
                  src={currentImage}
                  alt="Generated artwork"
                  className="w-full h-full object-contain"
                />
                {isGenerating && (
                  <div className="absolute inset-0 bg-background/80 backdrop-blur-sm flex items-center justify-center">
                    <div className="text-center space-y-4">
                      <Loader2 className="w-16 h-16 animate-spin text-primary mx-auto" />
                      <p className="text-muted-foreground">Finalizing...</p>
                    </div>
                  </div>
                )}
              </>
            ) : isGenerating ? (
              <div className="text-center space-y-4">
                <Loader2 className="w-16 h-16 animate-spin text-primary mx-auto" />
                <p className="text-muted-foreground">Creating your masterpiece...</p>
              </div>
            ) : (
              <div className="text-center space-y-2">
                <Sparkles className="w-16 h-16 text-muted-foreground/50 mx-auto" />
                <p className="text-muted-foreground">Your creation will appear here</p>
              </div>
            )}
          </div>

          {currentImage && !isGenerating && (
            <div className="mt-4 flex gap-3">
              <Button
                variant="default"
                className="flex-1"
                onClick={downloadImage}
              >
                <Download />
                Download
              </Button>
              <Button
                variant="secondary"
                className="flex-1"
                onClick={shareImage}
              >
                <Share2 />
                Share
              </Button>
            </div>
          )}
        </Card>

        {/* History */}
        {history.length > 0 && (
          <Card className="glass-card p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold flex items-center gap-2">
                <Clock className="w-5 h-5 text-primary" />
                Recent Generations ({history.length})
              </h2>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setHistory([])}
              >
                Clear History
              </Button>
            </div>
            <div className="grid grid-cols-3 md:grid-cols-4 gap-3">
              {history.map((image, idx) => (
                <button
                  key={idx}
                  onClick={() => loadHistoryImage(image)}
                  className="aspect-square rounded-lg overflow-hidden border-2 border-border hover:border-primary transition-all hover:scale-105"
                >
                  <img
                    src={image.url}
                    alt={image.prompt}
                    className="w-full h-full object-cover"
                  />
                </button>
              ))}
            </div>
          </Card>
        )}
      </div>
    </div>
  );
}
