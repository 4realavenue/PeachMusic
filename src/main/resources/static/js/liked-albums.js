import { authFetch } from "./auth.js";

const albumGrid = document.getElementById("albumGrid");
const loadingEl = document.getElementById("loading");
const endMessage = document.getElementById("endMessage");
const sentinel = document.getElementById("sentinel");

let lastLikeId = null;
let hasNext = true;
let isLoading = false;
let observer;

init();

async function init() {
    await load();
    setupInfiniteScroll();
}

async function load() {

    if (!hasNext || isLoading) return;

    isLoading = true;
    loadingEl.classList.remove("hidden");

    let url = "/api/users/likes/albums";
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

        if (page.nextCursor) {
            lastLikeId = page.nextCursor.lastId;
        }

        if (!hasNext) {
            endMessage.classList.remove("hidden");
            observer.disconnect();
        }

    } catch (e) {
        console.error(e);
    } finally {
        loadingEl.classList.add("hidden");
        isLoading = false;
    }
}

function render(list) {

    list.forEach(album => {

        const card = document.createElement("div");
        card.className = "album-card";
        card.style.cursor = "pointer";

        card.innerHTML = `
            <img class="album-image" src="${album.albumImage}" alt="앨범이미지">

            <div class="album-info">
                <div class="album-name">${album.albumName}</div>

                <div class="album-bottom">
                    <div class="like-count">
                        좋아요 <span>${album.likeCount ?? 0}</span>
                    </div>

                    <button class="heart-btn liked"
                            data-id="${album.albumId}">
                        ❤
                    </button>
                </div>
            </div>
        `;

        // ✅ 카드 클릭 → 앨범 단건조회(/page) 이동 (하트 클릭은 제외)
        card.addEventListener("click", (e) => {
            if (e.target.closest(".heart-btn")) return;
            location.href = `/albums/${album.albumId}/page`;
        });

        albumGrid.appendChild(card);
    });
}

/* 🔥 무한스크롤 */
function setupInfiniteScroll() {

    observer = new IntersectionObserver(async (entries) => {
        if (entries[0].isIntersecting) {
            await load();
        }
    }, {
        root: null,
        rootMargin: "300px",
        threshold: 0
    });

    observer.observe(sentinel);
}

/* 🔥 좋아요 토글 */
albumGrid.addEventListener("click", async (e) => {

    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    const albumId = heartBtn.dataset.id;

    try {
        const res = await authFetch(`/api/albums/${albumId}/likes`, {
            method: "POST"
        });

        const result = await res.json();
        if (!result.success) return;

        const { liked, likeCount } = result.data;

        heartBtn.classList.toggle("liked", liked);

        const likeText = heartBtn
            .closest(".album-bottom")
            .querySelector("span");

        likeText.textContent = likeCount;

    } catch (err) {
        console.error(err);
    }
});