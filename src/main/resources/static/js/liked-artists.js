import { authFetch } from "./auth.js";

const artistGrid = document.getElementById("artistGrid");
const loadingEl = document.getElementById("loading");
const endMessage = document.getElementById("endMessage");
const sentinel = document.getElementById("sentinel");

let lastLikeId = null;
let hasNext = true;
let isLoading = false;
let observer = null;

init();

/* =========================
   초기 실행
========================= */
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

    let url = "/api/users/likes/artists";
    if (lastLikeId !== null) {
        url += `?lastLikeId=${lastLikeId}`;
    }

    try {
        const res = await authFetch(url);
        const result = await res.json();

        if (!result.success) return;

        const page = result.data;

        render(page.content);

        hasNext = page.hasNext;

        if (page.nextCursor?.lastId != null) {
            lastLikeId = page.nextCursor.lastId;
        }

        if (!hasNext && observer) {
            endMessage.classList.remove("hidden");
            observer.disconnect();
        }

    } catch (e) {
        console.error("아티스트 로드 실패:", e);
    } finally {
        loadingEl.classList.add("hidden");
        isLoading = false;
    }
}

/* =========================
   렌더링
========================= */
function render(list) {

    list.forEach(artist => {

        const card = document.createElement("div");
        card.className = "artist-card";
        card.style.cursor = "pointer";
        card.dataset.id = artist.artistId;

        const imageElement = createImageOrPlaceholder(
            artist.profileImage,
            artist.artistName
        );

        card.appendChild(imageElement);

        const name = document.createElement("div");
        name.className = "artist-name";
        name.textContent = artist.artistName;

        const bottom = document.createElement("div");
        bottom.className = "artist-bottom";

        const likeCount = document.createElement("div");
        likeCount.className = "like-count";
        likeCount.innerHTML = `<span>${artist.likeCount ?? 0}</span>`;

        const heartBtn = document.createElement("button");
        heartBtn.className = "heart-btn liked";
        heartBtn.dataset.id = artist.artistId;
        heartBtn.textContent = "❤";

        bottom.appendChild(likeCount);
        bottom.appendChild(heartBtn);

        card.appendChild(name);
        card.appendChild(bottom);

        // ✅ 카드 클릭 → 아티스트 단건조회(page) 이동 (하트 클릭은 제외)
        card.addEventListener("click", (e) => {
            if (e.target.closest(".heart-btn")) return;
            location.href = `/artists/${artist.artistId}`;
        });

        artistGrid.appendChild(card);
    });
}

/* =========================
   이미지 or 기본 원형 생성
========================= */
function createImageOrPlaceholder(imageUrl, name) {

    if (imageUrl) {
        const img = document.createElement("img");
        img.className = "artist-image";
        img.src = imageUrl;
        img.alt = "아티스트 이미지";

        img.onerror = function () {
            this.replaceWith(createPlaceholder(name));
        };

        return img;
    }

    return createPlaceholder(name);
}

/* 기본 원형 아바타 */
function createPlaceholder(name) {

    const div = document.createElement("div");
    div.className = "artist-image placeholder";

    const firstChar = name ? name.charAt(0).toUpperCase() : "?";
    div.textContent = firstChar;

    return div;
}

/* =========================
   무한스크롤
========================= */
function setupInfiniteScroll() {

    observer = new IntersectionObserver(async (entries) => {

        if (entries[0].isIntersecting) {
            await load();
        }

    }, {
        root: null,
        rootMargin: "400px",
        threshold: 0
    });

    observer.observe(sentinel);
}

/* =========================
   좋아요 토글
========================= */
artistGrid.addEventListener("click", async (e) => {

    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    const artistId = heartBtn.dataset.id;

    try {
        const res = await authFetch(`/api/artists/${artistId}/likes`, {
            method: "POST"
        });

        const result = await res.json();
        if (!result.success) return;

        const { liked, likeCount } = result.data;

        heartBtn.classList.toggle("liked", liked);

        const likeText = heartBtn
            .closest(".artist-bottom")
            .querySelector("span");

        likeText.textContent = likeCount;

    } catch (err) {
        console.error("아티스트 좋아요 토글 실패:", err);
    }
});