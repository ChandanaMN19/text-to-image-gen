export default function handler(req, res) {
    res.setHeader('Access-Control-Allow-Origin', '*');
    if (req.method === 'DELETE') {
        return res.status(200).json({ message: "History cleared" });
    }
    return res.status(200).json([]);
}
