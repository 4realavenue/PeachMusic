import { authFetch } from "./auth.js";

const songList = document.getElementById("songList");
const loadingEl = document.getElementById("loading");
const endMessage = document.getElementById("endMessage");
const sentinel = document.getElementById("sentinel");

let lastLikeId = null;
let hasNext = true;
let isLoading = false;

init();

async function init() {
    await load();
    setupInfiniteScroll();
}

/* =========================
   데이터 로드 (Keyset)
========================= */
async function load() {

    if (!hasNext || isLoading) return;

    isLoading = true;
    loadingEl.classList.remove("hidden");

    let url = "/api/users/likes/songs";
    if (lastLikeId !== null) url += `?lastLikeId=${lastLikeId}`;

    try {
        const res = await authFetch(url);
        const result = await res.json();
        if (!result.success) return;

        const page = result.data;

        render(page.content);

        hasNext = page.hasNext;

        if (page.nextCursor && page.nextCursor.lastId != null) {
            lastLikeId = page.nextCursor.lastId;
        }

        if (!hasNext) {
            endMessage.classList.remove("hidden");
            observer && observer.disconnect();
        }

    } catch (e) {
        console.error("로드 실패:", e);
    } finally {
        loadingEl.classList.add("hidden");
        isLoading = false;
    }
}

/* =========================
   렌더링
========================= */
function render(list) {

    list.forEach(song => {

        const row = document.createElement("div");
        row.className = "liked-row";

        row.innerHTML = `
            <div class="col play">
                <button class="play-btn" data-audio="${song.audio}">▶</button>
            </div>

            <div class="col title">${song.name}</div>

            <div class="col like-count">
                <span class="like-number">${song.likeCount ?? 0}</span>
            </div>

            <div class="col heart">
                <button class="heart-btn liked" data-id="${song.songId}">❤</button>
            </div>
        `;

        songList.appendChild(row);
    });
}

/* =========================
   무한 스크롤 (IntersectionObserver)
========================= */
let observer = null;

function setupInfiniteScroll() {

    observer = new IntersectionObserver(async (entries) => {
        const entry = entries[0];
        if (!entry.isIntersecting) return;
        await load();
    }, {
        root: null,        // window 기준
        rootMargin: "300px", // 300px 전에 미리 로드
        threshold: 0
    });

    observer.observe(sentinel);
}

/* =========================
   좋아요 토글
========================= */
songList.addEventListener("click", async (e) => {

    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    const songId = heartBtn.dataset.id;

    try {
        const res = await authFetch(`/api/songs/${songId}/likes`, { method: "POST" });
        const result = await res.json();
        if (!result.success) return;

        const { liked, likeCount } = result.data;

        heartBtn.classList.toggle("liked", liked);
        heartBtn.textContent = liked ? "❤" : "🤍";

        const likeNumber = heartBtn.closest(".liked-row").querySelector(".like-number");
        likeNumber.textContent = likeCount;

    } catch (e2) {
        console.error("좋아요 토글 실패:", e2);
    }
});
