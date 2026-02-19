import { authFetch, getToken } from "/js/auth.js";

let lastId = null;
let lastSortValue = null;
let hasNext = true;
let loading = false;

const hasToken = !!getToken();

const grid = document.getElementById("albumGrid");
const title = document.getElementById("pageTitle");
const sentinel = document.getElementById("sentinel");
const loadingEl = document.getElementById("loading");
const endMessageEl = document.getElementById("endMessage");

/* =========================
   이미지 경로 안전 처리
========================= */
function resolveImageUrl(imagePath) {

    if (!imagePath) return "/images/default.png";

    // 외부 Open API URL
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
        return imagePath;
    }

    // 이미 / 로 시작하면 그대로
    if (imagePath.startsWith("/")) {
        return imagePath;
    }

    // 내부 업로드 경로
    return `/${imagePath}`;
}

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

    loadAlbums();

    const observer = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting && hasNext && !loading) {
            loadAlbums();
        }
    }, { threshold: 0.1 });

    observer.observe(sentinel);
});

/* =========================
   데이터 로드
========================= */
async function loadAlbums() {

    if (!hasNext) return;

    loading = true;
    loadingEl.classList.remove("hidden");

    const params = new URLSearchParams({
        word: initialWord,
        sortType: "LIKE",
        direction: "DESC"
    });

    if (lastId !== null) {
        params.append("lastId", lastId);
        params.append("lastLike", lastSortValue);
    }

    try {
        const res = await fetch(`/api/search/albums?${params}`);
        const response = await res.json();

        if (!response.success) return;

        const data = response.data;

        renderAlbums(data.content);

        hasNext = data.hasNext;

        if (hasNext && data.cursor) {
            lastId = data.cursor.lastId;
            lastSortValue = data.cursor.lastSortValue;
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
   카드 렌더링
========================= */
function renderAlbums(list) {

    list.forEach(album => {

        const card = document.createElement("div");
        card.className = "album-card";
        card.dataset.id = album.albumId;

        card.innerHTML = `
            <img src="${resolveImageUrl(album.albumImage)}" alt="album">

            <div class="album-name">${album.albumName}</div>

            <div class="album-artist">${album.artistName}</div>

            <div class="album-bottom">
                <span class="like-number">${album.likeCount ?? 0}</span>

                <button class="heart-btn 
                        ${album.liked ? 'liked' : ''} 
                        ${!hasToken ? 'disabled' : ''}"
                        data-id="${album.albumId}">
                    ❤
                </button>
            </div>
        `;

        grid.appendChild(card);
    });
}

/* =========================
   클릭 이벤트 통합 처리
========================= */
grid.addEventListener("click", async (e) => {

    // 1️⃣ 하트 클릭
    const heartBtn = e.target.closest(".heart-btn");

    if (heartBtn) {

        e.stopPropagation();

        if (!hasToken) {
            showLoginPopup();
            return;
        }

        const albumId = heartBtn.dataset.id;

        try {
            const res = await authFetch(`/api/albums/${albumId}/likes`, { method: "POST" });
            const result = await res.json();

            if (!result.success) return;

            const { liked, likeCount } = result.data;

            heartBtn.classList.toggle("liked", liked);

            const likeNumber = heartBtn
                .closest(".album-bottom")
                .querySelector(".like-number");

            likeNumber.textContent = likeCount;

        } catch (err) {
            console.error(err);
        }

        return;
    }

    // 2️⃣ 카드 클릭 → 앨범 단건 조회
    const card = e.target.closest(".album-card");
    if (!card) return;

    const albumId = card.dataset.id;
    if (!albumId) return;

    location.href = `/albums/${albumId}/page`;
});
