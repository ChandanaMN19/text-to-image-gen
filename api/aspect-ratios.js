export default function handler(req, res) {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.status(200).json([
        { name: "Square", value: "1:1", width: 1024, height: 1024 },
        { name: "Landscape", value: "16:9", width: 1024, height: 576 },
        { name: "Portrait", value: "9:16", width: 576, height: 1024 },
        { name: "Wide", value: "21:9", width: 1024, height: 439 }
    ]);
}
