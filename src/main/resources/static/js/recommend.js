import { authFetch } from "./auth.js";

const grid = document.getElementById("recommendGrid");
const loadingEl = document.getElementById("loading");
const emptyMessage = document.getElementById("emptyMessage");

init();

/* =========================
   추천 목록 불러오기
========================= */
async function init() {

    loadingEl.classList.remove("hidden");

    try {
        const res = await authFetch("/api/songs/recommendation");
        if (!res) return;

        const result = await res.json();

        if (!result.success) {
            showEmpty();
            return;
        }

        const list = result.data || [];

        if (!list.length) {
            showEmpty();
            return;
        }

        render(list);

    } catch (e) {
        console.error("추천 로드 실패:", e);
        showEmpty();
    } finally {
        loadingEl.classList.add("hidden");
    }
}

/* =========================
   카드 렌더링
========================= */
function render(list) {

    grid.innerHTML = "";

    list.forEach(song => {

        const card = document.createElement("div");
        card.className = "recommend-card";

        card.innerHTML = `
            <img src="${song.albumImage || '/images/default.png'}">
            <div class="song-name">${song.songName}</div>
            <div class="artist-name">${song.artistName}</div>
            <div class="bottom-row">
                <div class="like-text">
                    <span>${song.likeCount ?? 0}</span>
                </div>
                <button class="heart-btn"
                        data-id="${song.songId}">
                    ❤
                </button>
            </div>
        `;

        grid.appendChild(card);
    });
}

/* =========================
   좋아요 토글 (아티스트와 동일 방식)
========================= */
grid.addEventListener("click", async (e) => {

    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    const songId = heartBtn.dataset.id;

    try {
        const res = await authFetch(`/api/songs/${songId}/likes`, {
            method: "POST"
        });

        if (!res) return;

        const result = await res.json();
        if (!result.success) return;

        const { liked, likeCount } = result.data;

        // 🔥 클래스 기반 토글
        heartBtn.classList.toggle("liked", liked);

        // 좋아요 수 업데이트
        const likeText = heartBtn
            .closest(".bottom-row")
            .querySelector("span");

        likeText.textContent = likeCount;

    } catch (err) {
        console.error("좋아요 실패:", err);
    }
});

/* ========================= */
function showEmpty() {
    emptyMessage.classList.remove("hidden");
}
