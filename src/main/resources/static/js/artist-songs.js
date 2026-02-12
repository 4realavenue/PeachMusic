import { authFetch, getToken } from "/js/auth.js";

(() => {
    const artistId = document.getElementById("pageMeta")?.dataset?.artistId;

    const listEl = document.getElementById("songsList");
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

    function renderSongRow(s) {
        const songId = s.songId;
        const name = s.name ?? "음원";

        const row = document.createElement("div");
        row.className = "song-row";
        row.dataset.songId = songId;

        row.innerHTML = `
      <div>
        <div class="song-name">${escapeHtml(name)}</div>
        <div class="song-sub">${formatDate(s.albumReleaseDate)}</div>
      </div>

      <div class="like-number">${s.likeCount ?? 0}</div>

      <div class="like-area">
        <button class="heart-btn ${s.isLiked ? "liked" : ""} ${!hasToken ? "disabled" : ""}" data-id="${songId}">❤</button>
      </div>
    `;
        return row;
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
                ? `/api/artists/${artistId}/songs?${qs.toString()}`
                : `/api/artists/${artistId}/songs`;

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

            content.forEach((s) => listEl.appendChild(renderSongRow(s)));
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

    // ✅ row 클릭: 상세 이동 (하트 누르면 막기)
    listEl.addEventListener("click", (e) => {
        const heartBtn = e.target.closest(".heart-btn");
        if (heartBtn) return;
        const row = e.target.closest(".song-row");
        if (!row) return;
        const songId = row.dataset.songId;
        if (songId) window.location.href = `/songs/${songId}`;
    });

    // ✅ 좋아요 클릭
    listEl.addEventListener("click", async (e) => {
        const heartBtn = e.target.closest(".heart-btn");
        if (!heartBtn) return;

        e.stopPropagation();

        if (!hasToken) {
            showLoginPopup();
            return;
        }

        const songId = heartBtn.dataset.id;

        try {
            const res = await authFetch(`/api/songs/${songId}/likes`, { method: "POST" });
            const result = await res.json();
            if (!result.success) return;

            const { liked, likeCount } = result.data;

            heartBtn.classList.toggle("liked", liked);

            const row = heartBtn.closest(".song-row");
            const likeNumberEl = row?.querySelector(".like-number");
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
