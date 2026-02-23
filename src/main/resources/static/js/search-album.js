import { authFetch, getToken } from "/js/auth.js";

let cursor = null; // 🔥 통일
let hasNext = true;
let loading = false;

let currentSort = "LIKE";
let currentDirection = "DESC";

const grid = document.getElementById("albumGrid");
const title = document.getElementById("pageTitle");
const sentinel = document.getElementById("sentinel");
const loadingEl = document.getElementById("loading");
const endMessageEl = document.getElementById("endMessage");
const searchBtn = document.getElementById("searchBtn");
const searchInput = document.getElementById("searchInput");
const sortSelect = document.getElementById("sortSelect");
const directionSelect = document.getElementById("directionSelect");

/* =========================
   로그인 팝업
========================= */
function showLoginPopup() {
    const popup = document.getElementById("loginPopup");
    if (!popup) return;

    popup.classList.remove("hidden");
    popup.classList.add("show");

    setTimeout(() => {
        popup.classList.remove("show");
        popup.classList.add("hidden");
    }, 2000);
}

/* =========================
   초기 실행
========================= */
document.addEventListener("DOMContentLoaded", () => {

    if (!initialWord || initialWord.trim() === "") return;

    title.textContent = `"${initialWord}"와 관련된 앨범`;
    searchInput.value = initialWord;

    loadAlbums();

    const observer = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting && hasNext && !loading) {
            loadAlbums();
        }
    }, { threshold: 0.1 });

    observer.observe(sentinel);

    searchBtn?.addEventListener("click", handleSearch);
    searchInput?.addEventListener("keydown", e => {
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

/* =========================
   검색
========================= */
function handleSearch() {
    const word = searchInput.value.trim();
    if (!word) return;
    location.href = `/search/albums?word=${encodeURIComponent(word)}`;
}

/* =========================
   정렬 변경 시 초기화
========================= */
function resetAndReload() {
    cursor = null; // 🔥 통일
    hasNext = true;
    loading = false;
    grid.innerHTML = "";
    endMessageEl.classList.add("hidden");
    loadAlbums();
}

/* =========================
   데이터 로드
========================= */
async function loadAlbums() {

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
                case "PLAY":
                    params.append("lastPlay", cursor.lastSortValue);
                    break;
                case "RELEASE_DATE":
                    params.append("lastDate", cursor.lastSortValue);
                    break;
                case "NAME":
                    params.append("lastName", cursor.lastSortValue);
                    break;
            }
        }
    }

    try {

        const res = getToken()
            ? await authFetch(`/api/search/albums?${params}`)
            : await fetch(`/api/search/albums?${params}`);

        if (!res) return;

        const response = await res.json();
        if (!response.success) return;

        const data = response.data;

        renderAlbums(data.content);

        hasNext = data.hasNext;

        if (hasNext && data.cursor) {
            cursor = data.cursor; // 🔥 통일
        } else {
            endMessageEl.classList.remove("hidden");
        }

    } catch (err) {
        console.error(err);
    }

    loadingEl.classList.add("hidden");
    loading = false;
}

/* =========================
   렌더
========================= */
function renderAlbums(list) {

    list.forEach(album => {

        const card = document.createElement("div");
        card.className = "album-card";
        card.dataset.id = album.albumId;

        card.innerHTML = `
            <img src="${album.albumImage || '/images/default.png'}" alt="album">
            <div class="album-name">${album.albumName}</div>
            <div class="album-artist">${album.artistName}</div>

            <div class="album-bottom">
                <span class="like-number">${album.likeCount ?? 0}</span>
                <button class="heart-btn ${album.liked ? 'liked' : ''}"
                        data-id="${album.albumId}">
                    ❤
                </button>
            </div>
        `;

        grid.appendChild(card);
    });
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

        const albumId = heartBtn.dataset.id;

        try {

            const res = await authFetch(`/api/albums/${albumId}/likes`, {
                method: "POST"
            });

            if (!res) return;

            const result = await res.json();
            if (!result.success) return;

            const { liked, likeCount } = result.data;

            heartBtn.classList.toggle("liked", liked === true);

            const likeNumber = heartBtn
                .closest(".album-bottom")
                .querySelector(".like-number");

            likeNumber.textContent = likeCount ?? 0;

        } catch (err) {
            console.error(err);
        }

        return;
    }

    const card = e.target.closest(".album-card");
    if (!card) return;

    location.href = `/albums/${card.dataset.id}/page`;
});