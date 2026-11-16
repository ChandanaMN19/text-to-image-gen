const state = {
    selectedStyle: "Photorealistic",
    selectedRatio: "1:1",
    selectedEngine: "auto",
    currentImage: null,
    currentPrompt: "",
    isGenerating: false,
    cooldownRemaining: 0,
    cooldownInterval: null,
};

// DOM Elements
const promptInput = document.getElementById("promptInput");
const exampleChips = document.getElementById("exampleChips");
const stylesGrid = document.getElementById("stylesGrid");
const ratiosGrid = document.getElementById("ratiosGrid");
const enginesGrid = document.getElementById("enginesGrid");
const apiKeyContainer = document.getElementById("apiKeyContainer");
const customApiKey = document.getElementById("customApiKey");
const generateBtn = document.getElementById("generateBtn");
const btnText = document.getElementById("btnText");
const previewImg = document.getElementById("previewImg");
const previewPlaceholder = document.getElementById("previewPlaceholder");
const loadingOverlay = document.getElementById("loadingOverlay");
const loadingText = document.getElementById("loadingText");
const engineBadge = document.getElementById("engineBadge");
const cooldownBadge = document.getElementById("cooldownBadge");
const cooldownSeconds = document.getElementById("cooldownSeconds");
const actionBar = document.getElementById("actionBar");
const downloadBtn = document.getElementById("downloadBtn");
const shareBtn = document.getElementById("shareBtn");
const shareModal = document.getElementById("shareModal");
const closeModalBtn = document.getElementById("closeModalBtn");
const copyUrlBtn = document.getElementById("copyUrlBtn");
const whatsappBtn = document.getElementById("whatsappBtn");
const twitterBtn = document.getElementById("twitterBtn");
const facebookBtn = document.getElementById("facebookBtn");
const historyCard = document.getElementById("historyCard");
const historyGrid = document.getElementById("historyGrid");
const historyCount = document.getElementById("historyCount");
const clearHistoryBtn = document.getElementById("clearHistoryBtn");
const toastContainer = document.getElementById("toastContainer");

// Toast helper
function showToast(message, isError = false) {
    const toast = document.createElement("div");
    toast.className = `toast ${isError ? "error" : ""}`;
    toast.innerHTML = `<span>${isError ? "⚠️" : "✨"}</span><span>${message}</span>`;
    toastContainer.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = "0";
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Start Cooldown countdown
function startCooldown(seconds = 45) {
    state.cooldownRemaining = seconds;
    cooldownBadge.style.display = "inline-block";
    cooldownSeconds.textContent = state.cooldownRemaining;

    if (state.cooldownInterval) clearInterval(state.cooldownInterval);

    state.cooldownInterval = setInterval(() => {
        state.cooldownRemaining--;
        if (state.cooldownRemaining <= 0) {
            clearInterval(state.cooldownInterval);
            state.cooldownInterval = null;
            cooldownBadge.style.display = "none";
        } else {
            cooldownSeconds.textContent = state.cooldownRemaining;
        }
    }, 1000);
}

// Initial fetch
async function init() {
    setupEngineSelection();
    loadExamples();
    loadStyles();
    loadAspectRatios();
    loadHistory();
}

function setupEngineSelection() {
    const engineBtns = enginesGrid.querySelectorAll(".select-btn");
    engineBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            engineBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            state.selectedEngine = btn.getAttribute("data-engine");
            
            if (state.selectedEngine === "custom") {
                apiKeyContainer.style.display = "block";
            } else {
                apiKeyContainer.style.display = "none";
            }
        });
    });
}

// Example Prompts
async function loadExamples() {
    try {
        const res = await fetch("/api/examples");
        const examples = await res.json();
        exampleChips.innerHTML = "";
        examples.forEach(ex => {
            const btn = document.createElement("button");
            btn.className = "example-btn";
            btn.textContent = ex.length > 36 ? ex.slice(0, 36) + "..." : ex;
            btn.title = ex;
            btn.onclick = () => {
                promptInput.value = ex;
            };
            exampleChips.appendChild(btn);
        });
    } catch (e) {
        console.error("Failed to load examples", e);
    }
}

// Art Styles
async function loadStyles() {
    try {
        const res = await fetch("/api/styles");
        const styles = await res.json();
        stylesGrid.innerHTML = "";
        styles.forEach((st, idx) => {
            const btn = document.createElement("button");
            btn.className = `select-btn ${idx === 0 ? "active" : ""}`;
            btn.textContent = st.name;
            btn.onclick = () => {
                document.querySelectorAll("#stylesGrid .select-btn").forEach(b => b.classList.remove("active"));
                btn.classList.add("active");
                state.selectedStyle = st.name;
            };
            stylesGrid.appendChild(btn);
        });
    } catch (e) {
        console.error("Failed to load styles", e);
    }
}

// Aspect Ratios
async function loadAspectRatios() {
    try {
        const res = await fetch("/api/aspect-ratios");
        const ratios = await res.json();
        ratiosGrid.innerHTML = "";
        ratios.forEach((ar, idx) => {
            const btn = document.createElement("button");
            btn.className = `select-btn ${idx === 0 ? "active" : ""}`;
            btn.textContent = `${ar.name} (${ar.value})`;
            btn.onclick = () => {
                document.querySelectorAll("#ratiosGrid .select-btn").forEach(b => b.classList.remove("active"));
                btn.classList.add("active");
                state.selectedRatio = ar.value;
            };
            ratiosGrid.appendChild(btn);
        });
    } catch (e) {
        console.error("Failed to load aspect ratios", e);
    }
}

// Generate Image
async function generateImage() {
    const prompt = promptInput.value.trim();
    if (!prompt) {
        showToast("Please enter a prompt first!", true);
        return;
    }

    state.isGenerating = true;
    generateBtn.disabled = true;
    btnText.textContent = "Generating Magic...";
    loadingOverlay.classList.add("active");
    loadingText.textContent = "Creating your masterpiece...";

    try {
        const response = await fetch("/api/generate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                prompt: prompt,
                style: state.selectedStyle,
                aspectRatio: state.selectedRatio,
                provider: state.selectedEngine,
                apiKey: customApiKey.value.trim() || null,
            }),
        });

        const data = await response.json();

        if (!response.ok || !data.success) {
            throw new Error(data.error || "Failed to generate image");
        }

        state.currentImage = data.imageUrl;
        state.currentPrompt = prompt;

        // Check if Pollinations was used, trigger cooldown indicator
        if (data.engine && data.engine.includes("Pollinations")) {
            startCooldown(45);
        }

        // Show engine badge
        if (data.engine) {
            engineBadge.textContent = data.engine;
            engineBadge.style.display = "inline-block";
        }

        // Preload image before displaying
        const tempImg = new Image();
        tempImg.onload = () => {
            previewPlaceholder.style.display = "none";
            previewImg.src = data.imageUrl;
            previewImg.style.display = "block";
            actionBar.style.display = "flex";
            loadingOverlay.classList.remove("active");
            state.isGenerating = false;
            generateBtn.disabled = false;
            btnText.textContent = "Generate Image";
            showToast(`Generated successfully via ${data.engine || "AI"}!`);
            loadHistory();
        };
        tempImg.onerror = () => {
            previewPlaceholder.style.display = "none";
            previewImg.src = data.imageUrl;
            previewImg.style.display = "block";
            actionBar.style.display = "flex";
            loadingOverlay.classList.remove("active");
            state.isGenerating = false;
            generateBtn.disabled = false;
            btnText.textContent = "Generate Image";
            showToast("Image ready!");
            loadHistory();
        };
        tempImg.src = data.imageUrl;

    } catch (err) {
        console.error(err);
        showToast(err.message || "An error occurred", true);
        loadingOverlay.classList.remove("active");
        state.isGenerating = false;
        generateBtn.disabled = false;
        btnText.textContent = "Generate Image";
    }
}

// Download Image
function downloadImage() {
    if (!state.currentImage) return;
    const downloadUrl = `/api/download?url=${encodeURIComponent(state.currentImage)}`;
    const a = document.createElement("a");
    a.href = downloadUrl;
    a.download = `ai-generated-${Date.now()}.png`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    showToast("Downloading image...");
}

// Share Modal
function openShareModal() {
    shareModal.classList.add("active");
}

function closeShareModal() {
    shareModal.classList.remove("active");
}

function copyImageUrl() {
    if (!state.currentImage) return;
    navigator.clipboard.writeText(state.currentImage).then(() => {
        showToast("Image URL copied to clipboard!");
        closeShareModal();
    });
}

function shareWhatsApp() {
    if (!state.currentImage) return;
    const text = `Check out this AI-generated image: ${state.currentPrompt} ${state.currentImage}`;
    window.open(`https://wa.me/?text=${encodeURIComponent(text)}`, "_blank");
    closeShareModal();
}

function shareTwitter() {
    if (!state.currentImage) return;
    const text = `Check out this AI-generated image: ${state.currentPrompt}`;
    window.open(`https://twitter.com/intent/tweet?text=${encodeURIComponent(text)}&url=${encodeURIComponent(state.currentImage)}`, "_blank");
    closeShareModal();
}

function shareFacebook() {
    if (!state.currentImage) return;
    window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(state.currentImage)}`, "_blank");
    closeShareModal();
}

// History
async function loadHistory() {
    try {
        const res = await fetch("/api/history");
        const list = await res.json();
        if (list && list.length > 0) {
            historyCard.style.display = "block";
            historyCount.textContent = list.length;
            historyGrid.innerHTML = "";
            list.forEach(item => {
                const thumb = document.createElement("div");
                thumb.className = "history-thumb";
                thumb.title = `${item.prompt} (${item.style})`;
                thumb.innerHTML = `<img src="${item.url}" alt="${item.prompt}" loading="lazy" />`;
                thumb.onclick = () => {
                    previewPlaceholder.style.display = "none";
                    previewImg.src = item.url;
                    previewImg.style.display = "block";
                    promptInput.value = item.prompt;
                    state.currentImage = item.url;
                    state.currentPrompt = item.prompt;
                    actionBar.style.display = "flex";
                    // select matching style
                    document.querySelectorAll("#stylesGrid .select-btn").forEach(b => {
                        if (b.textContent === item.style) b.classList.add("active");
                        else b.classList.remove("active");
                    });
                    state.selectedStyle = item.style;
                    window.scrollTo({ top: previewImg.offsetTop - 100, behavior: "smooth" });
                };
                historyGrid.appendChild(thumb);
            });
        } else {
            historyCard.style.display = "none";
        }
    } catch (e) {
        console.error("Failed to load history", e);
    }
}

async function clearHistory() {
    try {
        await fetch("/api/history", { method: "DELETE" });
        loadHistory();
        showToast("History cleared");
    } catch (e) {
        showToast("Failed to clear history", true);
    }
}

// Event Listeners
generateBtn.addEventListener("click", generateImage);
downloadBtn.addEventListener("click", downloadImage);
shareBtn.addEventListener("click", openShareModal);
closeModalBtn.addEventListener("click", closeShareModal);
copyUrlBtn.addEventListener("click", copyImageUrl);
whatsappBtn.addEventListener("click", shareWhatsApp);
twitterBtn.addEventListener("click", shareTwitter);
facebookBtn.addEventListener("click", shareFacebook);
clearHistoryBtn.addEventListener("click", clearHistory);

shareModal.addEventListener("click", (e) => {
    if (e.target === shareModal) closeShareModal();
});

// Run on load
document.addEventListener("DOMContentLoaded", init);
