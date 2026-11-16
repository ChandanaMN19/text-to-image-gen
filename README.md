# AI Text-to-Image Generator (Java Edition) 🎨✨

A complete, high-performance **Java Spring Boot 3** application for generating AI images from text prompts with customizable art styles, aspect ratios, instant image downloads, social sharing, and **persistent SQL database storage**.

---

## 🌟 Key Features

- **Java 21 / 24 & Spring Boot 3**: Fully modern Java backend with RESTful API architecture.
- **Persistent SQL Database (H2 + Spring Data JPA)**:
  - All generated images, prompts, styles, engines, and timestamps are **automatically saved to disk** (`./data/imagedb.mv.db`).
  - History is permanently preserved across server restarts.
  - Built-in **H2 Database Web Console** at `http://localhost:8080/h2-console` to view and query tables directly.
- **Dual Interface**:
  - 🌐 **Web UI (`http://localhost:8080`)**: Beautiful cosmic glassmorphism dark theme with live preview, multi-engine switcher, animations, and toast notifications.
  - 🖥️ **Desktop Swing GUI**: Standalone Java Desktop application with canvas rendering and native file export.
- **Smart Multi-Engine AI Support**:
  - 🤖 **Auto (Smart Fallback)**: Automatically switches engines to bypass rate limits.
  - ⚡ **AI Horde (Stable Diffusion)**: Unlimited generations with **no 45-second cooldown** and **zero API key required**.
  - 🌸 **Pollinations AI (Flux)**: High-detail model with a live visual 45-second cooldown timer.
  - 🔑 **Custom API Key**: Supports Gemini / OpenAI / Lovable API keys for private generation.
- **9 Art Styles & 4 Aspect Ratios**: Photorealistic, Anime, Digital Art, Oil Painting, Watercolor, Sketch, Cyberpunk, Fantasy, Minimalist; Square (1:1), Landscape (16:9), Portrait (9:16), Wide (21:9).
- **Download & Share**: One-click PNG download, URL copy, and quick share to WhatsApp, Twitter/X, and Facebook.

---

## 🗄️ Database Details

- **Database Engine**: H2 Database (Embedded, File-based SQL database)
- **File Location**: `./data/imagedb.mv.db` (survives restarts)
- **Table Name**: `GENERATED_IMAGES`
- **Schema**:
  | Column | Type | Description |
  |---|---|---|
  | `ID` | BIGINT (Primary Key, Auto-increment) | Unique generation ID |
  | `PROMPT` | VARCHAR(2000) | User text prompt |
  | `IMAGE_URL` | VARCHAR(4000) | Generated image URL |
  | `ART_STYLE` | VARCHAR(255) | Selected art style |
  | `ASPECT_RATIO` | VARCHAR(255) | Aspect ratio (1:1, 16:9, etc.) |
  | `AI_ENGINE` | VARCHAR(255) | Engine used to generate |
  | `TIMESTAMP` | BIGINT | Millisecond epoch timestamp |

### 🛠️ Access the H2 Database Web Console
1. Open your browser and visit: **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**
2. Set JDBC URL to: `jdbc:h2:file:./data/imagedb`
3. Username: `sa`, Password: *(leave blank)*
4. Click **Connect** and run SQL queries like:
   ```sql
   SELECT * FROM GENERATED_IMAGES ORDER BY TIMESTAMP DESC;
   ```

---

## 🚀 Quick Start

### Prerequisites
- **Java JDK 21+** (or Java 24) installed (`java -version`)
- **Apache Maven 3.8+** installed (`mvn -version`)

---

### Option 1: Run the Web Application (Recommended)

1. Open your terminal in this project directory:
   ```bash
   mvn spring-boot:run
   ```
   *(Or double-click `run.bat` on Windows)*

2. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

---

### Option 2: Run the Java Desktop GUI (Swing)

```bash
mvn compile exec:java -Dexec.mainClass="com.imageng.gui.ImageGeneratorSwingApp"
```
*(Or double-click `run-desktop.bat` on Windows)*

---

### Option 3: Run Executable JAR

```bash
java -jar target/text-to-image-gen-1.0.0.jar
```

---

## 📡 REST API Documentation

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/generate` | Generates an AI image and persists it to the database |
| `GET` | `/api/history` | Retrieves generation history from the database (newest first) |
| `DELETE` | `/api/history` | Clears all history from the database |
| `GET` | `/api/styles` | Returns all available art styles |
| `GET` | `/api/aspect-ratios` | Returns supported aspect ratios |
| `GET` | `/api/examples` | Returns starter sample prompts |
| `GET` | `/api/download?url=...` | Proxies and downloads image with attachment headers |
| `GET` | `/api/health` | Health check endpoint |
