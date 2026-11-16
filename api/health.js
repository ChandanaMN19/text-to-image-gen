export default function handler(req, res) {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.status(200).json({
        status: "UP",
        platform: "Vercel Serverless + Java Spring Boot",
        timestamp: Date.now()
    });
}
