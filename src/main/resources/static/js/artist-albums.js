import { authFetch, getToken } from "/js/auth.js";

(() => {
    const artistId = document.getElementById("pageMeta")?.dataset?.artistId;

    const gridEl = document.getElementById("albumsGrid");
    const loadingText = document.getElementById("loadingText");
    const emptyText = document.getElementById("emptyText");
    const endText = document.getElementById("endText");
    const errorText = document.getElementById("errorText");

    const sentinel = document.getElementById("sentinel");
    const popup = document.getElementById("loginPopup");

    const hasToken = !!getToken();

    let nextCursor = null;
    let isLoading = false;
    let reachedEnd = false;
    let loadedAny = false;

    function resolveImageUrl(imagePath) {
        if (!imagePath) return "/images/default.png";
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath;
        if (imagePath.startsWith("/")) return imagePath;
        return `/${imagePath}`;
    }

    function setLoading(on) {
        isLoading = on;
        loadingText?.classList.toggle("hidden", !on);
    }
    function hideStatus() {
        emptyText?.classList.add("hidden");
        endText?.classList.add("hidden");
        errorText?.classList.add("hidden");
    }
    function showEmpty() { emptyText?.classList.remove("hidden"); }
    function showEnd() { endText?.classList.remove("hidden"); }
    function showError() { errorText?.classList.remove("hidden"); }

    function showLoginPopup() {
        if (!popup) return;
        popup.classList.remove("hidden");
        popup.classList.add("show");
        setTimeout(() => {
            popup.classList.remove("show");
            popup.classList.add("hidden");
        }, 2000);
    }

    function buildQueryFromCursor(cursorObj) {
        const params = new URLSearchParams();
        if (!cursorObj || typeof cursorObj !== "object") return params;
        for (const [k, v] of Object.entries(cursorObj)) {
            if (v === null || v === undefined) continue;
            params.append(k, String(v));
        }
        return params;
    }

    function escapeHtml(str) {
        return String(str ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function formatDate(dateStr) {
        if (!dateStr) return "";
        const d = new Date(dateStr);
        if (isNaN(d)) return String(dateStr);
        return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")}`;
    }

    function renderAlbumCard(a) {
        const albumId = a.albumId;
        const albumName = a.albumName ?? "앨범";
        const coverUrl = resolveImageUrl(a.albumImage);

        const card = document.createElement("div");
        card.className = "album-card";
        card.dataset.albumId = albumId;

        card.innerHTML = `
            <div class="albums-album-cover" style="background-image:url('${coverUrl}')"></div>
            <div class="album-info">
                <div class="album-name">${escapeHtml(albumName)}</div>

                <div class="album-meta">
                  <span>${formatDate(a.albumReleaseDate)}</span>

                    <div class="album-like-area">
                      <span class="like-number">${a.likeCount ?? 0}</span>
                      <button class="heart-btn ${a.liked ? "liked" : ""} ${!hasToken ? "disabled" : ""}" data-id="${albumId}">
                        ❤
                      </button>
                    </div>

                </div>
              </div>
            `;

            return card;
    }

    async function request(url) {
        const token = localStorage.getItem("accessToken");
        if (token) return authFetch(url, { method: "GET" });
        return fetch(url, { method: "GET" });
    }

    async function fetchNext() {
        if (isLoading || reachedEnd) return;
        hideStatus();
        setLoading(true);

        try {
            const qs = buildQueryFromCursor(nextCursor);
            const url = qs.toString()
                ? `/api/artists/${artistId}/albums?${qs.toString()}`
                : `/api/artists/${artistId}/albums`;

            const res = await request(url);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            const payload = await res.json();
            const data = payload?.data;
            const content = data?.content ?? [];
            const newCursor = data?.nextCursor ?? null;

            if (content.length === 0 && !loadedAny) {
                showEmpty();
                reachedEnd = true;
                return;
            }

            content.forEach((a) => gridEl.appendChild(renderAlbumCard(a)));
            loadedAny = loadedAny || content.length > 0;

            if (!newCursor || content.length === 0) {
                reachedEnd = true;
                showEnd();
            } else {
                nextCursor = newCursor;
            }
        } catch (e) {
            console.error(e);
            showError();
        } finally {
            setLoading(false);
        }
    }

    // ✅ 카드 클릭: 상세 이동
    gridEl.addEventListener("click", (e) => {
        const heartBtn = e.target.closest(".heart-btn");
        if (heartBtn) return; // 하트는 별도 처리
        const card = e.target.closest(".album-card");
        if (!card) return;
        const albumId = card.dataset.albumId;
        if (albumId) window.location.href = `/albums/${albumId}/page`;
    });

    // ✅ 좋아요 클릭
    gridEl.addEventListener("click", async (e) => {
        const heartBtn = e.target.closest(".heart-btn");
        if (!heartBtn) return;

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
            const likeNumberEl = heartBtn.closest(".album-meta")?.querySelector(".like-number");
            if (likeNumberEl) likeNumberEl.textContent = likeCount ?? 0;
        } catch (err) {
            console.error(err);
        }
    });

    // ✅ 무한스크롤
    const io = new IntersectionObserver(
        (entries) => {
            if (entries.some((en) => en.isIntersecting)) fetchNext();
        },
        { root: null, rootMargin: "600px 0px", threshold: 0.01 }
    );

    io.observe(sentinel);
    fetchNext();
})();