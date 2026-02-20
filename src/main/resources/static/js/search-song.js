import { authFetch, getToken } from "./auth.js";

let lastId = null;
let hasNext = true;
let loading = false;

let currentSort = "LIKE";
let currentDirection = "DESC";

const listContainer = document.getElementById("songList");
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

document.addEventListener("DOMContentLoaded", () => {

    if (!initialWord || initialWord.trim() === "") return;

    title.textContent = `"${initialWord}"와 관련된 음원`;

    loadSongs();
    setupInfiniteScroll();

    searchBtn?.addEventListener("click", () => {
        const word = searchInput.value.trim();
        if (!word) return;
        location.href = `/search/songs?word=${encodeURIComponent(word)}`;
    });

    sortSelect?.addEventListener("change", () => {
        currentSort = sortSelect.value;
        currentDirection = currentSort === "NAME" ? "ASC" : "DESC";
        directionSelect.value = currentDirection;
        resetAndReload();
    });

    directionSelect?.addEventListener("change", () => {
        currentDirection = directionSelect.value;
        resetAndReload();
    });
});

/* =========================
   무한스크롤
========================= */
function setupInfiniteScroll() {

    observer = new IntersectionObserver(async (entries) => {
        if (entries[0].isIntersecting) {
            await loadSongs();
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
    loading = false;
    listContainer.innerHTML = "";
    endMessage.classList.add("hidden");
    loadSongs();
}

/* =========================
   데이터 로드
========================= */
async function loadSongs() {

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

        const res = getToken()
            ? await authFetch(`/api/search/songs?${params}`)
            : await fetch(`/api/search/songs?${params}`);

        if (!res) return;

        const response = await res.json();
        if (!response.success) return;

        const data = response.data;

        renderSongs(data.content);

        hasNext = data.hasNext;

        if (hasNext && data.nextCursor) {
            lastId = data.nextCursor.lastId;
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
function renderSongs(list) {

    list.forEach(song => {

        const row = document.createElement("div");
        row.className = "song-row";

        row.innerHTML = `
            <img src="${song.albumImage || '/images/default.png'}">
            <div class="song-name">${song.name}</div>
            <div>${song.artistName}</div>

            <div class="like-area">
                <span class="like-number">${song.likeCount ?? 0}</span>

                <button class="heart-btn 
                        ${song.liked ? 'liked' : ''}"
                        data-id="${song.songId}">
                    ❤
                </button>
            </div>
        `;

        listContainer.appendChild(row);

        row.addEventListener("click", (e) => {
            if (e.target.closest(".heart-btn")) return;
            location.href = `/songs/${song.songId}/page`;
        });
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
   좋아요 토글 (안정화 버전)
========================= */
listContainer.addEventListener("click", async (e) => {

    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    e.stopPropagation();

    if (!getToken()) {
        showLoginPopup();
        return;
    }

    const songId = heartBtn.dataset.id;

    try {

        const res = await authFetch(`/api/songs/${songId}/likes`, {
            method: "POST"
        });

        if (!res) return;

        const result = await res.json();
        if (!result.success) return;

        const { liked, likeCount } = result.data;

        /* 🔥ㅁㅍㅁ 즉시 UI 반영 */
        heartBtn.classList.toggle("liked", liked === true);

        const likeNumber = heartBtn
            .closest(".song-row")
            .querySelector(".like-number");

        likeNumber.textContent = likeCount ?? 0;

    } catch (err) {
        console.error(err);
    }
});