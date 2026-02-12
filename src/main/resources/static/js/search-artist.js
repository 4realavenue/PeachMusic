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

        card.innerHTML = `
            <div class="artist-img">
                <img src="${artist.profileImage || '/images/default.png'}">
            </div>

            <div class="artist-name">${artist.artistName}</div>

            <div class="artist-bottom">
                <span class="like-number">${artist.likeCount ?? 0}</span>

                <button class="heart-btn 
                        ${artist.liked ? 'liked' : ''} 
                        ${!hasToken ? 'disabled' : ''}"
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

    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    e.stopPropagation();

    if (!hasToken) {
        showLoginPopup();
        return;
    }

    const artistId = heartBtn.dataset.id;

    try {
        const res = await authFetch(`/api/artists/${artistId}/likes`, {
            method: "POST"
        });

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
});
