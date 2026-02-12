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

const hasToken = !!getToken();
let observer = null;

document.addEventListener("DOMContentLoaded", () => {

    if (!initialWord || initialWord.trim() === "") return;

    title.textContent = `"${initialWord}"와 관련된 곡`;

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
        const res = await fetch(`/api/search/songs?${params}`);
        const response = await res.json();
        if (!response.success) return;

        const data = response.data;

        renderSongs(data.content);

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
                        ${song.liked ? 'liked' : ''} 
                        ${!hasToken ? 'disabled' : ''}"
                        data-id="${song.songId}">
                    ❤
                </button>
            </div>
        `;

        listContainer.appendChild(row);
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

listContainer.addEventListener("click", async (e) => {

    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    e.stopPropagation();

    if (!hasToken) {
        showLoginPopup();
        return;
    }

    const songId = heartBtn.dataset.id;

    try {
        const res = await authFetch(`/api/songs/${songId}/likes`, {
            method: "POST"
        });

        const result = await res.json();
        if (!result.success) return;

        const { liked, likeCount } = result.data;

        heartBtn.classList.toggle("liked", liked);

        const likeNumber = heartBtn
            .closest(".song-row")
            .querySelector(".like-number");

        likeNumber.textContent = likeCount;

    } catch (err) {
        console.error(err);
    }
});
