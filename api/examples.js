export default function handler(req, res) {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.status(200).json([
        "A majestic phoenix rising from cosmic flames in space",
        "Cyberpunk cityscape at night with neon reflections",
        "Ancient magical library with floating books and crystals",
        "Underwater kingdom with bioluminescent creatures",
        "Steampunk airship floating above Victorian clouds"
    ]);
}
