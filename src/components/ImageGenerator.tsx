import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { Loader2, Sparkles, Download, Share2, Clock, Copy, Facebook, Twitter } from "lucide-react";
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
  { name: "Sketch", value: "pencil sketch, hand drawn, artistic sketch, line art" },
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
  const [shareSheetOpen, setShareSheetOpen] = useState(false);

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

  const copyImageUrl = async () => {
    if (!currentImage) return;
    
    try {
      await navigator.clipboard.writeText(currentImage);
      toast.success("Image copied to clipboard!");
      setShareSheetOpen(false);
    } catch (error) {
      toast.error("Failed to copy image");
    }
  };

  const shareToWhatsApp = () => {
    if (!currentImage) return;
    const text = `Check out this AI-generated image: ${prompt}`;
    window.open(`https://wa.me/?text=${encodeURIComponent(text + " " + currentImage)}`, '_blank');
    setShareSheetOpen(false);
  };

  const shareToFacebook = () => {
    if (!currentImage) return;
    window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(currentImage)}`, '_blank');
    setShareSheetOpen(false);
  };

  const shareToTwitter = () => {
    if (!currentImage) return;
    const text = `Check out this AI-generated image: ${prompt}`;
    window.open(`https://twitter.com/intent/tweet?text=${encodeURIComponent(text)}&url=${encodeURIComponent(currentImage)}`, '_blank');
    setShareSheetOpen(false);
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
              <Sheet open={shareSheetOpen} onOpenChange={setShareSheetOpen}>
                <SheetTrigger asChild>
                  <Button
                    variant="secondary"
                    className="flex-1"
                  >
                    <Share2 />
                    Share
                  </Button>
                </SheetTrigger>
                <SheetContent>
                  <SheetHeader>
                    <SheetTitle>Share Image</SheetTitle>
                  </SheetHeader>
                  <div className="mt-6 space-y-3">
                    <Button
                      variant="outline"
                      className="w-full justify-start gap-3"
                      onClick={copyImageUrl}
                    >
                      <Copy className="w-5 h-5" />
                      Copy to Clipboard
                    </Button>
                    <Button
                      variant="outline"
                      className="w-full justify-start gap-3"
                      onClick={shareToWhatsApp}
                    >
                      <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"/>
                      </svg>
                      Share on WhatsApp
                    </Button>
                    <Button
                      variant="outline"
                      className="w-full justify-start gap-3"
                      onClick={shareToFacebook}
                    >
                      <Facebook className="w-5 h-5" />
                      Share on Facebook
                    </Button>
                    <Button
                      variant="outline"
                      className="w-full justify-start gap-3"
                      onClick={shareToTwitter}
                    >
                      <Twitter className="w-5 h-5" />
                      Share on Twitter
                    </Button>
                    <Button
                      variant="outline"
                      className="w-full justify-start gap-3"
                      onClick={downloadImage}
                    >
                      <Download className="w-5 h-5" />
                      Download Image
                    </Button>
                  </div>
                </SheetContent>
              </Sheet>
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
