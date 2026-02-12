import { authFetch, getToken } from "./auth.js";

let lastId = null;
let hasNext = true;
let loading = false;

let currentSort = "LIKE";
let currentDirection = "DESC";

const grid = document.getElementById("artistGrid");
const title = document.getElementById("pageTitle");
const searchBtn = document.getElementById("searchBtn");
const searchInput = document.getElementById("searchInput");
const sortSelect = document.getElementById("sortSelect");
const directionSelect = document.getElementById("directionSelect");

const sentinel = document.getElementById("sentinel");
const loadingEl = document.getElementById("loading");
const endMessage = document.getElementById("endMessage");
const popup = document.getElementById("loginPopup");

const hasToken = !!getToken();
let observer = null;

/* =========================
   이미지 경로 처리
   - Open API 아티스트는 이미지 없음(null) → placeholder 시도
   - placeholder도 막히면 onerror로 로컬 기본이미지로 대체
========================= */
function resolveImageUrl(imagePath) {
    // ✅ 외부 placeholder (placeholder.com은 막히는 경우가 있어 placehold.co 추천)
    if (!imagePath) return "https://placehold.co/300x300?text=artist";

    // ✅ 외부 URL이면 그대로 사용
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath;

    // ✅ 이미 / 로 시작하면 그대로
    if (imagePath.startsWith("/")) return imagePath;

    // ✅ 내부 경로(uploads 등)라면 / 붙여서 사용
    return `/${imagePath}`;
}

document.addEventListener("DOMContentLoaded", () => {
    if (!initialWord || initialWord.trim() === "") return;

    title.textContent = `"${initialWord}"와 관련된 아티스트`;

    loadArtists();
    setupInfiniteScroll();

    searchBtn.addEventListener("click", () => {
        const word = searchInput.value.trim();
        if (!word) return;
        location.href = `/search/artists?word=${encodeURIComponent(word)}`;
    });

    sortSelect.addEventListener("change", () => {
        currentSort = sortSelect.value;
        resetAndReload();
    });

    directionSelect.addEventListener("change", () => {
        currentDirection = directionSelect.value;
        resetAndReload();
    });
});

function setupInfiniteScroll() {
    observer = new IntersectionObserver(async (entries) => {
        if (entries[0].isIntersecting) {
            await loadArtists();
        }
    }, {
        root: null,
        rootMargin: "300px",
        threshold: 0
    });

    observer.observe(sentinel);
}

function resetAndReload() {
    lastId = null;
    hasNext = true;
    grid.innerHTML = "";
    endMessage.classList.add("hidden");
    loadArtists();
}

async function loadArtists() {
    if (!hasNext || loading) return;

    loading = true;
    loadingEl.classList.remove("hidden");

    const params = new URLSearchParams({
        word: initialWord,
        sortType: currentSort,
        direction: currentDirection
    });

    if (lastId !== null) {
        params.append("lastId", lastId);
    }

    try {
        const res = await fetch(`/api/search/artists?${params}`);
        const response = await res.json();
        if (!response.success) return;

        const data = response.data;

        renderArtists(data.content);

        hasNext = data.hasNext;

        if (hasNext && data.cursor) {
            lastId = data.cursor.lastId;
        } else {
            endMessage.classList.remove("hidden");
            observer?.disconnect();
        }
    } catch (e) {
        console.error(e);
    }

    loadingEl.classList.add("hidden");
    loading = false;
}

function renderArtists(list) {
    list.forEach(artist => {
        const card = document.createElement("div");
        card.className = "artist-card";
        card.dataset.artistId = artist.artistId;

        const imgUrl = resolveImageUrl(artist.profileImage);

        card.innerHTML = `
            <div class="artist-img">
                <img
                    src="${imgUrl}"
                    alt="artist"
                    onerror="this.onerror=null; this.src='/images/default-artist.png';"
                >
            </div>

            <div class="artist-name">${artist.artistName}</div>

            <div class="artist-bottom">
                <span class="like-number">${artist.likeCount ?? 0}</span>

                <button class="heart-btn
                        ${artist.liked ? "liked" : ""}
                        ${!hasToken ? "disabled" : ""}"
                        data-id="${artist.artistId}">
                    ❤
                </button>
            </div>
        `;

        grid.appendChild(card);
    });
}

function showLoginPopup() {
    popup.classList.remove("hidden");
    popup.classList.add("show");

    setTimeout(() => {
        popup.classList.remove("show");
        popup.classList.add("hidden");
    }, 2000);
}

grid.addEventListener("click", async (e) => {

    // 1) 하트 클릭이면: 좋아요 토글
    const heartBtn = e.target.closest(".heart-btn");
    if (heartBtn) {
        e.stopPropagation();

        if (!hasToken) {
            showLoginPopup();
            return;
        }

        const artistId = heartBtn.dataset.id;

        try {
            const res = await authFetch(`/api/artists/${artistId}/likes`, { method: "POST" });
            if (!res) return;

            const result = await res.json();
            if (!result.success) return;

            const { liked, likeCount } = result.data;

            heartBtn.classList.toggle("liked", liked);

            const likeNumber = heartBtn
                .closest(".artist-bottom")
                .querySelector(".like-number");

            likeNumber.textContent = likeCount;

        } catch (err) {
            console.error(err);
        }

        return; // ✅ 하트 처리 끝나면 상세이동 막기
    }

    // 2) 그 외(카드 클릭)이면: artist-detail로 이동
    const card = e.target.closest(".artist-card");
    if (!card) return;

    const artistId = card.dataset.artistId;
    if (!artistId) return;

    window.location.href = `/artists/${artistId}`;
});
