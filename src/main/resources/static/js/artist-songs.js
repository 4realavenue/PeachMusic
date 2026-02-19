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

    /* =========================
       UI helpers
    ========================= */
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

    /* =========================
       Cursor
    ========================= */
    function buildQueryFromCursor(cursorObj) {
        const params = new URLSearchParams();
        if (!cursorObj || typeof cursorObj !== "object") return params;
        for (const [k, v] of Object.entries(cursorObj)) {
            if (v === null || v === undefined) continue;
            params.append(k, String(v));
        }
        return params;
    }

    /* =========================
       Utils
    ========================= */
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
        return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(
            d.getDate()
        ).padStart(2, "0")}`;
    }

    /* =========================
       Request
    ========================= */
    async function request(url) {
        if (getToken()) return authFetch(url, { method: "GET" });
        return fetch(url, { method: "GET" });
    }

    /* =========================
       Render
    ========================= */
    function renderSongRow(s) {
        const songId = s.songId;
        const name = s.name ?? "음원";

        const row = document.createElement("div");
        row.className = "song-row";
        row.dataset.songId = String(songId);

        row.innerHTML = `
      <div class="song-left">
        <div class="song-name">${escapeHtml(name)}</div>
        <div class="song-sub">${formatDate(s.albumReleaseDate)}</div>
      </div>

      <div class="song-actions">
        <button class="track-play" type="button" aria-label="재생" data-id="${songId}">▶</button>

        <span class="like-group">
          <span class="like-number">${s.likeCount ?? 0}</span>
          <button class="heart-btn ${s.isLiked ? "liked" : ""} ${!hasToken ? "disabled" : ""}"
                  type="button"
                  aria-label="음원 좋아요"
                  data-id="${songId}">❤</button>
        </span>
      </div>
    `;

        return row;
    }

    /* =========================
       Global player sync
    ========================= */
    function getGlobalAudioEl() {
        return document.querySelector(".player audio") || document.getElementById("audioPlayer") || null;
    }

    function isSameTrack(globalAudio, url) {
        if (!globalAudio || !globalAudio.src || !url) return false;
        const currentFile = String(globalAudio.src).split("/").pop();
        const nextFile = String(url).split("/").pop();
        return currentFile && nextFile && currentFile === nextFile;
    }

    function setPlayBtnState(btn, isPlaying) {
        if (!btn) return;
        btn.classList.toggle("playing", isPlaying);
        btn.textContent = isPlaying ? "❚❚" : "▶";
        btn.setAttribute("aria-label", isPlaying ? "일시정지" : "재생");
    }

    async function fetchAudioUrl(songId) {
        const res = await request(`/api/songs/${songId}/play`);
        if (!res || !res.ok) return null;

        let payload = null;
        try { payload = await res.json(); } catch { payload = null; }

        if (!payload?.success) return null;
        return payload.data?.streamingUrl ?? null;
    }

    function wireGlobalAudioSync() {
        const globalAudio = getGlobalAudioEl();
        if (!globalAudio) return;

        const sync = () => {
            document.querySelectorAll(".song-row").forEach((row) => {
                const btn = row.querySelector(".track-play");
                if (!btn) return;

                const url = btn.dataset.audioUrl || null;
                if (!url) {
                    setPlayBtnState(btn, false);
                    return;
                }

                const same = isSameTrack(globalAudio, url);
                const isPlaying = same && !globalAudio.paused;
                setPlayBtnState(btn, isPlaying);
            });
        };

        globalAudio.addEventListener("play", sync);
        globalAudio.addEventListener("pause", sync);
        globalAudio.addEventListener("ended", sync);
    }

    /* =========================
       Pagination fetch
    ========================= */
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

    /* =========================
       Events
    ========================= */

    // ✅ row 클릭: 상세 이동 (재생/하트 누르면 막기)
    listEl.addEventListener("click", (e) => {
        if (e.target.closest(".track-play")) return;
        if (e.target.closest(".heart-btn")) return;

        const row = e.target.closest(".song-row");
        if (!row) return;
        const songId = row.dataset.songId;
        if (songId) window.location.href = `/songs/${songId}/page`;
    });

    // ✅ 하트 좋아요
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
            if (!res) return;

            const result = await res.json();
            if (!result?.success) return;

            const { liked, likeCount } = result.data;

            heartBtn.classList.toggle("liked", !!liked);

            const row = heartBtn.closest(".song-row");
            const likeNumberEl = row?.querySelector(".like-number");
            if (likeNumberEl) likeNumberEl.textContent = String(likeCount ?? 0);
        } catch (err) {
            console.error(err);
        }
    });

    // ✅ 재생: 전역 플레이어 호출
    listEl.addEventListener("click", async (e) => {
        const playBtn = e.target.closest(".track-play");
        if (!playBtn) return;

        e.stopPropagation();

        const songId = Number(playBtn.dataset.id);
        if (!songId) return;

        if (typeof window.playSongFromPage !== "function") {
            alert("전역 플레이어가 아직 로드되지 않았습니다.");
            return;
        }

        const url = await fetchAudioUrl(songId);
        if (!url) {
            alert("재생 가능한 음원 주소가 없습니다.");
            return;
        }

        // 버튼에 저장(전역 오디오와 같은 곡인지 비교/싱크용)
        playBtn.dataset.audioUrl = url;

        const title =
            playBtn.closest(".song-row")?.querySelector(".song-name")?.textContent ?? "Unknown";

        try {
            await window.playSongFromPage(url, title, songId);
        } catch (err) {
            console.error(err);
            alert("재생에 실패했습니다.");
        }
    });

    /* =========================
       Infinite scroll
    ========================= */
    const io = new IntersectionObserver(
        (entries) => {
            if (entries.some((en) => en.isIntersecting)) fetchNext();
        },
        { root: null, rootMargin: "600px 0px", threshold: 0.01 }
    );

    if (sentinel) io.observe(sentinel);

    wireGlobalAudioSync();
    fetchNext();
})();