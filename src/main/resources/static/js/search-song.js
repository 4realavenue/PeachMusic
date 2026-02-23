import { authFetch, getToken } from "./auth.js";

let cursor = null; // 🔥 수정 (기존 lastId → cursor 객체)
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
    cursor = null; // 🔥 수정
    hasNext = true;
    loading = false;
    listContainer.innerHTML = "";
    endMessage.classList.add("hidden");
    observer?.disconnect();
    setupInfiniteScroll();
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

    // 🔥 수정: lastId + 정렬기준 커서값 함께 전송
    if (cursor?.lastId != null) {
        params.append("lastId", cursor.lastId);
    }

    if (cursor?.lastSortValue != null) {

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
            cursor = data.nextCursor; // 🔥 수정
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
   렌더 (✅ 2줄: 제목 / 아티스트·앨범)
========================= */
function renderSongs(list) {

    list.forEach(song => {

        const row = document.createElement("div");
        row.className = "song-row";
        row.dataset.id = song.songId;

        const img = (song.albumImage || "/images/default.png");
        const name = (song.name ?? "-");
        const artist = (song.artistName ?? "-");
        const album = (song.albumName ?? "-"); // ✅ 응답에 있음(없으면 '-' 처리)
        const likeCount = (song.likeCount ?? 0);

        row.innerHTML = `
            <img src="${img}" alt=""
                 onerror="this.onerror=null; this.src='/images/default.png';">

            <div class="song-info">
                <div class="song-name">${escapeHtml(name)}</div>
                <div class="song-sub">${escapeHtml(artist)} · ${escapeHtml(album)}</div>
            </div>

            <div class="like-area">
                <span class="like-number">${likeCount}</span>

                <button class="heart-btn ${song.liked ? "liked" : ""}"
                        data-id="${song.songId}"
                        type="button"
                        aria-label="좋아요">
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

/**
 * ✅ 특수문자(&quot; 등) 깨짐 방지
 * 서버에서 이미 escape된 문자열이 들어올 수 있어
 * 프론트에서 또 escape하면 &amp;quot;로 이중 변환되어 화면에 그대로 노출됨.
 * => 여기서는 추가 escape 하지 않고 그대로 반환
 */
function escapeHtml(str) {
    return String(str ?? "");
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
   좋아요 토글
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

        heartBtn.classList.toggle("liked", liked === true);

        const likeNumber = heartBtn
            .closest(".song-row")
            .querySelector(".like-number");

        likeNumber.textContent = likeCount ?? 0;

    } catch (err) {
        console.error(err);
    }
});