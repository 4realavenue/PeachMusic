import { authFetch, getToken } from "./auth.js";

let cursor = null; // 🔥 통일
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

let observer = null;

/* =========================
   이미지 경로 처리
========================= */
function resolveImageUrl(imagePath) {
    if (!imagePath) return "https://placehold.co/300x300?text=artist";
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath;
    if (imagePath.startsWith("/")) return imagePath;
    return `/${imagePath}`;
}

/* =========================
   초기 실행
========================= */
document.addEventListener("DOMContentLoaded", () => {

    if (!initialWord || initialWord.trim() === "") return;

    title.textContent = `"${initialWord}"와 관련된 아티스트`;
    searchInput.value = initialWord;

    loadArtists();
    setupInfiniteScroll();

    searchBtn?.addEventListener("click", handleSearch);
    searchInput?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") handleSearch();
    });

    sortSelect?.addEventListener("change", () => {
        currentSort = sortSelect.value;
        resetAndReload();
    });

    directionSelect?.addEventListener("change", () => {
        currentDirection = directionSelect.value;
        resetAndReload();
    });
});

/* ========================= */
function handleSearch() {
    const word = searchInput.value.trim();
    if (!word) return;
    location.href = `/search/artists?word=${encodeURIComponent(word)}`;
}

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
    cursor = null; // 🔥 통일
    hasNext = true;
    loading = false;
    grid.innerHTML = "";
    endMessage.classList.add("hidden");
    loadArtists();
}

/* =========================
   데이터 로드
========================= */
async function loadArtists() {

    if (!hasNext || loading) return;

    loading = true;
    loadingEl.classList.remove("hidden");

    const params = new URLSearchParams({
        word: initialWord,
        sortType: currentSort,
        direction: currentDirection
    });

    // 🔥 통일된 cursor 구조
    if (cursor?.lastId != null) {

        params.append("lastId", cursor.lastId);

        if (cursor.lastSortValue != null) {
            switch (currentSort) {
                case "LIKE":
                    params.append("lastLike", cursor.lastSortValue);
                    break;

                case "NAME":
                    params.append("lastName", cursor.lastSortValue);
                    break;
            }
        }
    }

    try {

        const res = getToken()
            ? await authFetch(`/api/search/artists?${params}`)
            : await fetch(`/api/search/artists?${params}`);

        if (!res) return;

        const response = await res.json();
        if (!response.success) return;

        const data = response.data;

        renderArtists(data.content);

        hasNext = data.hasNext;

        if (hasNext && data.cursor) {
            cursor = data.cursor; // 🔥 통일
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

/* =========================
   렌더
========================= */
function renderArtists(list) {

    list.forEach(artist => {

        const card = document.createElement("div");
        card.className = "artist-card";
        card.dataset.artistId = artist.artistId;

        const imgUrl = resolveImageUrl(artist.profileImage);

        card.innerHTML = `
            <div class="artist-img">
                <img src="${imgUrl}"
                     alt="artist"
                     onerror="this.onerror=null; this.src='/images/default-artist.png';">
            </div>

            <div class="artist-name">${artist.artistName}</div>

            <div class="artist-bottom">
                <span class="like-number">${artist.likeCount ?? 0}</span>

                <button class="heart-btn ${artist.liked ? "liked" : ""}"
                        data-id="${artist.artistId}">
                    ❤
                </button>
            </div>
        `;

        grid.appendChild(card);
    });
}

/* =========================
   로그인 팝업
========================= */
function showLoginPopup() {
    popup.classList.remove("hidden");
    popup.classList.add("show");

    setTimeout(() => {
        popup.classList.remove("show");
        popup.classList.add("hidden");
    }, 2000);
}

/* =========================
   클릭 처리
========================= */
grid.addEventListener("click", async (e) => {

    const heartBtn = e.target.closest(".heart-btn");

    if (heartBtn) {

        e.stopPropagation();

        if (!getToken()) {
            showLoginPopup();
            return;
        }

        const artistId = heartBtn.dataset.id;

        try {

            const res = await authFetch(`/api/artists/${artistId}/likes`, {
                method: "POST"
            });

            if (!res) return;

            const result = await res.json();
            if (!result.success) return;

            const { liked, likeCount } = result.data;

            heartBtn.classList.toggle("liked", liked === true);

            const likeNumber = heartBtn
                .closest(".artist-bottom")
                .querySelector(".like-number");

            likeNumber.textContent = likeCount ?? 0;

        } catch (err) {
            console.error(err);
        }

        return;
    }

    const card = e.target.closest(".artist-card");
    if (!card) return;

    window.location.href = `/artists/${card.dataset.artistId}`;
});