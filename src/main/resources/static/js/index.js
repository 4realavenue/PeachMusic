import { authFetch, getToken } from "/js/auth.js";
import { resolveAudioUrl } from "/js/player-hls.js";

const listEl = document.getElementById("songsList");
const errorBox = document.getElementById("errorBox");
const loadingBox = document.getElementById("loadingBox");

const sortSelect = document.getElementById("sortSelect");
const directionSelect = document.getElementById("directionSelect");

const moreBtn = document.getElementById("moreBtn");
const endMsg = document.getElementById("endMsg");
const sentinel = document.getElementById("sentinel");

const sortStatus = document.getElementById("sortStatus");
const toTopBtn = document.getElementById("toTopBtn");

/* ✅ 추천 */
const recommendGrid = document.getElementById("recommendGrid");

const PAGE_SIZE = 15;

let state = {
    sortType: "LIKE",
    direction: "DESC",
    loading: false,
    hasNext: true,
    cursor: null,
    observer: null,
};

/* =========================
   UI helpers
========================= */
function showLoading(on) {
    if (!loadingBox) return;
    loadingBox.classList.toggle("hidden", !on);
}
function showError(msg) {
    if (!errorBox) return;
    errorBox.classList.remove("hidden")
    errorBox.textContent = msg;
}
function hideError() {
    if (!errorBox) return;
    errorBox.classList.add("hidden");
    errorBox.textContent = "";
}
function setEnd(show) {
    if (endMsg) endMsg.classList.toggle("hidden", !show);
    if (moreBtn) moreBtn.disabled = show || !state.hasNext || state.loading;
}

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
   ✅ HTML 엔티티 디코딩 (&amp; 등)
========================= */
function decodeEntities(str) {
    if (str == null) return "";
    const txt = document.createElement("textarea");
    txt.innerHTML = String(str);
    return txt.value;
}

/* XSS 방지 escape */
function escapeHtml(str) {
    return String(str ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

/* 이미지 URL 보정 */
function resolveImageUrl(imagePath) {
    if (!imagePath) return "/images/default.png";
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath;
    if (imagePath.startsWith("/")) return imagePath;
    return `/${imagePath}`;
}

/* 날짜 포맷 */
function formatDate(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    if (isNaN(d)) return String(dateStr);
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")}`;
}

function sortLabel(type) {
    switch (type) {
        case "LIKE": return "좋아요";
        case "PLAY": return "재생수";
        case "RELEASE_DATE": return "발매일";
        case "NAME": return "이름";
        default: return String(type ?? "정렬");
    }
}
function dirLabel(dir) {
    return dir === "ASC" ? "오름차순" : "내림차순";
}
function updateSortStatus() {
    if (!sortStatus) return;
    sortStatus.textContent = `${sortLabel(state.sortType)} · ${dirLabel(state.direction)}`;
}

async function request(url) {
    return getToken() ? authFetch(url, { method: "GET" }) : fetch(url, { method: "GET" });
}

/**
 * ✅ nextCursor={lastId,lastSortValue} -> CursorParam(lastLike/lastName/lastDate/lastPlay) 매핑
 */
function buildListUrl(cursor) {
    const params = new URLSearchParams();
    params.set("sortType", state.sortType);
    params.set("direction", state.direction);
    params.set("size", String(PAGE_SIZE));

    if (cursor?.lastId != null) params.set("lastId", String(cursor.lastId));

    if (cursor?.lastSortValue != null) {
        const v = cursor.lastSortValue;
        switch (state.sortType) {
            case "LIKE": params.set("lastLike", String(v)); break;
            case "PLAY": params.set("lastPlay", String(v)); break;
            case "RELEASE_DATE": params.set("lastDate", String(v)); break;
            case "NAME": params.set("lastName", String(v)); break;
            default: break;
        }
    }

    return `/api/songs?${params.toString()}`;
}

async function fetchPage(cursor) {
    const url = buildListUrl(cursor);
    const res = await request(url);

    if (!res || !res.ok) {
        const text = await res?.text().catch(() => "");
        throw new Error(`HTTP ${res?.status} ${text}`);
    }

    const payload = await res.json();
    if (!payload?.success) throw new Error(payload?.message || "조회 실패");

    const data = payload.data;
    return {
        content: data?.content ?? [],
        hasNext: data?.hasNext === true,
        nextCursor: data?.nextCursor ?? null,
    };
}

/* =========================
   ✅ 추천 5개 로드
========================= */
function createRecommendCard(song) {
    const div = document.createElement("div");
    div.className = "reco-card";

    const imgSrc = resolveImageUrl(song.albumImage);
    const title = escapeHtml(decodeEntities(song.songName ?? song.name ?? "-"));
    const artist = escapeHtml(decodeEntities(song.artistName ?? "-"));

    div.innerHTML = `
    <div class="reco-img">
      <img src="${escapeHtml(imgSrc)}" alt="" loading="lazy"
           onerror="this.onerror=null; this.src='/images/default.png';">
    </div>
    <div class="reco-body">
      <p class="reco-title">${title}</p>
      <p class="reco-sub">${artist}</p>
    </div>
  `;

    const songId = song.songId ?? song.id;
    div.addEventListener("click", () => {
        if (songId) location.href = `/songs/${songId}/page`;
    });

    return div;
}

async function loadRecommendTop5() {
    if (!recommendGrid) return;

    // 추천 API가 인증 기반이면 토큰 없을 때 빈칸
    if (!getToken()) {
        recommendGrid.innerHTML = "";
        return;
    }

    try {
        const res = await authFetch("/api/songs/recommendation");
        if (!res) return;

        const json = await res.json();
        if (!json?.success) return;

        const list = Array.isArray(json.data) ? json.data : [];
        recommendGrid.innerHTML = "";
        list.slice(0, 5).forEach((song) => recommendGrid.appendChild(createRecommendCard(song)));
    } catch (e) {
        console.error("추천 로딩 실패:", e);
        recommendGrid.innerHTML = "";
    }
}

/* =========================
   목록 렌더
========================= */
function appendRows(items) {
    const hasToken = !!getToken();

    items.forEach((s) => {
        const row = document.createElement("div");
        row.className = "song-row";
        row.dataset.songId = String(s.songId);

        const imgSrc = resolveImageUrl(s.albumImage);

        const safeName = escapeHtml(decodeEntities(s.name ?? "-"));
        const safeArtist = escapeHtml(decodeEntities(s.artistName ?? "-"));

        row.innerHTML = `
      <div class="thumb">
        <img src="${escapeHtml(imgSrc)}" alt="" loading="lazy"
             onerror="this.onerror=null; this.src='/images/default.png';">
      </div>

      <div class="song-main">
        <div class="song-name">${safeName}</div>
        <div class="song-sub">${formatDate(s.releaseDate)} · ${safeArtist}</div>
      </div>

      <div class="song-actions">
        <button class="track-play" type="button" aria-label="재생" data-id="${s.songId}">▶</button>
        <span class="like-number">${s.likeCount ?? 0}</span>
        <button class="heart-btn ${s.liked ? "liked" : ""} ${!hasToken ? "disabled" : ""}"
                type="button"
                aria-label="좋아요"
                data-id="${s.songId}">❤</button>
      </div>
    `;

        listEl.appendChild(row);
    });

    syncPlayButtons();
}

function scrollToTopSmooth() {
    window.scrollTo({ top: 0, behavior: "smooth" });
}

async function loadMore() {
    if (state.loading || !state.hasNext) return;

    state.loading = true;
    hideError();
    showLoading(true);
    if (moreBtn) moreBtn.disabled = true;

    try {
        const { content, hasNext, nextCursor } = await fetchPage(state.cursor);

        if (!Array.isArray(content) || content.length === 0) {
            state.hasNext = false;
            setEnd(true);
            return;
        }

        appendRows(content);

        state.hasNext = hasNext;
        state.cursor = nextCursor;

        setEnd(!state.hasNext);
    } catch (e) {
        console.error(e);
        showError("음원 목록을 불러오지 못했습니다. (다시 시도해 주세요)");
    } finally {
        state.loading = false;
        showLoading(false);
        if (moreBtn) moreBtn.disabled = !state.hasNext;
    }
}

function resetAndReload() {
    if (state.observer) {
        try { state.observer.disconnect(); } catch (_) {}
        state.observer = null;
    }

    listEl.innerHTML = "";
    hideError();
    showLoading(false);

    state.cursor = null;
    state.hasNext = true;
    state.loading = false;

    updateSortStatus();
    setEnd(false);
    initObserver();

    scrollToTopSmooth();
    loadMore();
}

/* =========================
   ✅ "현재 화면 DOM 기준" 큐 생성 + setPlayerQueue 등록
========================= */
function buildQueueFromDom() {
    const rows = Array.from(document.querySelectorAll(".song-row[data-song-id]"));
    return rows
        .map((row) => {
            const songId = Number(row.dataset.songId);
            const title = row.querySelector(".song-name")?.textContent?.trim() || "Unknown";
            if (!Number.isFinite(songId)) return null;
            return { songId, title };
        })
        .filter(Boolean);
}

function setGlobalQueueFromThisPage(startSongId) {
    const tracks = buildQueueFromDom();
    if (!tracks.length) return;

    // ✅ 통일: setPlayerQueue만 사용
    if (typeof window.setPlayerQueue === "function") {
        window.setPlayerQueue(tracks, Number(startSongId), {
            loop: true,
            contextKey: `songs:list:${state.sortType}:${state.direction}`,
        });
    }
}

/* =========================
   전역 플레이어 sync
========================= */
function setPlayBtnState(btn, isPlaying) {
    if (!btn) return;
    btn.classList.toggle("playing", isPlaying);
    btn.textContent = isPlaying ? "❚❚" : "▶";
    btn.setAttribute("aria-label", isPlaying ? "일시정지" : "재생");
}

function getGlobalAudioEl() {
    return document.querySelector(".player audio") || document.getElementById("audioPlayer") || null;
}
function isSameTrack(globalAudio, url) {
    if (!globalAudio || !globalAudio.src || !url) return false;
    const currentFile = String(globalAudio.src).split("/").pop();
    const nextFile = String(url).split("/").pop();
    return currentFile && nextFile && currentFile === nextFile;
}
function syncPlayButtons() {
    const globalAudio = getGlobalAudioEl();
    document.querySelectorAll(".song-row").forEach((row) => {
        const btn = row.querySelector(".track-play");
        if (!btn) return;

        const url = btn.dataset.audioUrl || null;
        if (!url || !globalAudio) {
            setPlayBtnState(btn, false);
            return;
        }

        const same = isSameTrack(globalAudio, url);
        setPlayBtnState(btn, same && !globalAudio.paused);
    });
}

/* =========================
   Infinite scroll
========================= */
function initObserver() {
    if (!sentinel) return;

    state.observer = new IntersectionObserver(
        (entries) => {
            const entry = entries[0];
            if (!entry?.isIntersecting) return;
            loadMore();
        },
        { root: null, rootMargin: "220px 0px", threshold: 0 }
    );

    state.observer.observe(sentinel);
}

/* =========================
   Top button
========================= */
function updateTopBtn() {
    if (!toTopBtn) return;
    const show = window.scrollY > 500;
    toTopBtn.classList.toggle("hidden", !show);
}

/* =========================
   Events
========================= */

// 상세 이동
listEl.addEventListener("click", (e) => {
    if (e.target.closest(".track-play")) return;
    if (e.target.closest(".heart-btn")) return;

    const row = e.target.closest(".song-row");
    if (!row) return;

    const songId = row.dataset.songId;
    if (songId) location.href = `/songs/${songId}/page`;
});

// 재생 (✅ /play + resolveAudioUrl + ✅ 큐 등록 + playSongFromPage 3args)
listEl.addEventListener("click", async (e) => {
    const btn = e.target.closest(".track-play");
    if (!btn) return;

    e.stopPropagation();

    if (typeof window.playSongFromPage !== "function") {
        alert("전역 플레이어가 아직 로드되지 않았습니다.");
        return;
    }

    const songId = Number(btn.dataset.id);
    if (!Number.isFinite(songId)) return;

    try {
        const res = await request(`/api/songs/${songId}/play`);
        if (!res) return;

        const payload = await res.json().catch(() => null);

        if (!res.ok || payload?.success === false) {
            alert(payload?.message || "재생 가능한 음원이 없습니다.");
            return;
        }

        const raw = payload?.data?.streamingUrl ?? null;
        const url = resolveAudioUrl(raw);
        if (!url) {
            alert("재생 가능한 음원 주소가 없습니다.");
            return;
        }

        btn.dataset.audioUrl = url;

        const title =
            btn.closest(".song-row")?.querySelector(".song-name")?.textContent?.trim() || "Unknown";

        // ✅ 먼저 큐 등록
        setGlobalQueueFromThisPage(songId);

        // ✅ 통일: 3 args
        await window.playSongFromPage(url, title, songId);
        syncPlayButtons();
    } catch (err) {
        console.error(err);
        alert("재생에 실패했습니다.");
    }
});

// like
listEl.addEventListener("click", async (e) => {
    const heartBtn = e.target.closest(".heart-btn");
    if (!heartBtn) return;

    e.stopPropagation();

    // 🔥 수정: disabled 버튼은 완전 차단
    if (heartBtn.hasAttribute("disabled")) return;

    if (!getToken()) {
        showLoginPopup();
        return;
    }

    const songId = heartBtn.dataset.id;

    try {
        const res = await authFetch(`/api/songs/${songId}/likes`, { method: "POST" });
        if (!res) return;

        const payload = await res.json();
        if (!payload?.success) return;

        const { liked, likeCount } = payload.data;

        heartBtn.classList.toggle("liked", liked === true);

        const likeNumEl = heartBtn.closest(".song-row")?.querySelector(".like-number");
        if (likeNumEl) likeNumEl.textContent = String(likeCount ?? 0);
    } catch (err) {
        console.error(err);
    }
});

// 더보기
moreBtn?.addEventListener("click", () => loadMore());

// 정렬 변경
sortSelect?.addEventListener("change", () => {
    state.sortType = sortSelect.value || "LIKE";
    resetAndReload();
});
directionSelect?.addEventListener("change", () => {
    state.direction = directionSelect.value || "DESC";
    resetAndReload();
});

// 맨 위로
toTopBtn?.addEventListener("click", () => scrollToTopSmooth());
window.addEventListener("scroll", updateTopBtn, { passive: true });

// init
document.addEventListener("DOMContentLoaded", async () => {
    state.sortType = sortSelect?.value || "LIKE";
    state.direction = directionSelect?.value || "DESC";

    updateSortStatus();
    updateTopBtn();

    const token = localStorage.getItem("accessToken"); // ✅ sidebar.js와 동일 기준
    const recommendSection = document.getElementById("recommendSection");

    // ✅ 비로그인: 추천 섹션 숨김(또는 remove)
    if (!token) {
        if (recommendSection) recommendSection.remove(); // 숨기고 싶으면 .classList.add("hidden")
    } else {
        // ✅ 로그인: 추천 5개 로드
        await loadRecommendTop5();
    }

    initObserver();
    loadMore();

    const globalAudio = getGlobalAudioEl();
    if (globalAudio) {
        globalAudio.addEventListener("play", syncPlayButtons);
        globalAudio.addEventListener("pause", syncPlayButtons);
        globalAudio.addEventListener("ended", syncPlayButtons);
    }
});