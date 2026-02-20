import { authFetch, getToken } from "./auth.js";
import { resolveAudioUrl } from "/js/player-hls.js";

const listEl = document.getElementById("chartList");
const errorBox = document.getElementById("errorBox");
const prevBtn = document.getElementById("prevBtn");
const nextBtn = document.getElementById("nextBtn");
const pageInfo = document.getElementById("pageInfo");
const loginPopup = document.getElementById("loginPopup");

const TOP100_API = "/api/songs/ranking/Top100";
const SONG_DETAIL_API = (songId) => `/api/songs/${songId}`;
const SONG_PLAY_API = (songId) => `/api/songs/${songId}/play`;
const SONG_LIKE_API = (songId) => `/api/songs/${songId}/likes`;
const SONG_PAGE_URL = (songId) => `/songs/${songId}/page`;

const PAGE_SIZE = 10;

let top = [];
let page = 0;

let currentSongId = null;
let currentPlayBtn = null;

/* =========================
   ✅ 현재 페이지(10곡) 기준 큐 생성
========================= */
function buildChartTracksForCurrentPage() {
    const start = page * PAGE_SIZE;
    const items = top.slice(start, start + PAGE_SIZE);

    return items
        .map((row) => ({
            songId: Number(row.id),
            title: decodeHtmlEntities(row.title ?? "Unknown"),
        }))
        .filter((t) => Number.isFinite(t.songId));
}

/* =========================
   Global Player
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

function wireGlobalAudioSync() {
    const globalAudio = getGlobalAudioEl();
    if (!globalAudio) return;

    const sync = () => {
        document.querySelectorAll(".chart-item").forEach((li) => {
            const btn = li.querySelector(".track-play");
            if (!btn) return;

            const url = btn.dataset.audioUrl || null;
            const same = url ? isSameTrack(globalAudio, url) : false;
            const isPlaying = same && !globalAudio.paused;

            setPlayBtnState(btn, isPlaying);
        });

        if (currentPlayBtn) {
            const url = currentPlayBtn.dataset.audioUrl || null;
            const same = url ? isSameTrack(globalAudio, url) : false;
            if (!same) {
                currentSongId = null;
                currentPlayBtn = null;
            }
        }
    };

    globalAudio.addEventListener("play", sync);
    globalAudio.addEventListener("pause", sync);
    globalAudio.addEventListener("ended", sync);

    sync();
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

function decodeHtmlEntities(str) {
    const txt = document.createElement("textarea");
    txt.innerHTML = String(str ?? "");
    return txt.value;
}

async function request(url) {
    if (getToken()) return authFetch(url, { method: "GET" });
    return fetch(url, { method: "GET" });
}

/* =========================
   Login popup
========================= */
function showLoginPopup() {
    if (!loginPopup) return;

    loginPopup.classList.remove("hidden");
    loginPopup.classList.add("show");

    setTimeout(() => {
        loginPopup.classList.remove("show");
        loginPopup.classList.add("hidden");
    }, 2000);
}

/* =========================
   Init
========================= */
document.addEventListener("DOMContentLoaded", async () => {
    wireGlobalAudioSync();
    await loadTop100();

    prevBtn?.addEventListener("click", () => {
        if (page === 0) return;
        page--;
        render();
        window.scrollTo({ top: 0, behavior: "smooth" });
    });

    nextBtn?.addEventListener("click", () => {
        if (page >= totalPages() - 1) return;
        page++;
        render();
        window.scrollTo({ top: 0, behavior: "smooth" });
    });
});

async function loadTop100() {
    hideError();
    listEl.innerHTML = "";

    try {
        const res = await fetch(TOP100_API);
        const payload = await res.json();

        if (!res.ok || payload?.success === false) {
            showError(payload?.message ?? "차트 조회에 실패했습니다.");
            return;
        }

        top = Array.isArray(payload.data) ? payload.data : [];
        if (top.length === 0) {
            showError("차트 데이터가 없습니다.");
            return;
        }

        page = 0;
        render();
    } catch (e) {
        console.error(e);
        showError("서버 오류가 발생했습니다.");
    }
}

function render() {
    listEl.innerHTML = "";

    const hasToken = !!getToken();

    const start = page * PAGE_SIZE;
    const items = top.slice(start, start + PAGE_SIZE);

    pageInfo.textContent = `${page + 1} / ${totalPages()}`;
    prevBtn.disabled = page === 0;
    nextBtn.disabled = page === totalPages() - 1;

    items.forEach((row, idx) => {
        const rank = start + idx + 1;

        const songId = row.id;
        const title = decodeHtmlEntities(row.title ?? "-");

        const li = document.createElement("li");
        li.className = "chart-item";
        li.dataset.songId = String(songId);

        li.innerHTML = `
          <div class="rank-pill">${rank}</div>

          <div class="thumb">
            <img src="/images/default.png" alt="album"
                 onerror="this.src='/images/default.png'">
          </div>

          <div class="info">
            <div class="song-title">${escapeHtml(title)}</div>
          </div>

          <div class="right-actions">
            <button class="track-play" type="button" aria-label="재생">▶</button>

            <div class="like-group">
              <span class="like-number">0</span>
              <button class="heart-btn ${!hasToken ? "disabled" : ""}" type="button" aria-label="좋아요">❤</button>
            </div>
          </div>
        `;

        // 행 클릭 -> 단건조회 (/page)
        li.addEventListener("click", (e) => {
            if (e.target.closest(".track-play")) return;
            if (e.target.closest(".heart-btn")) return;
            location.href = SONG_PAGE_URL(songId);
        });

        // 재생
        const playBtn = li.querySelector(".track-play");
        playBtn.addEventListener("click", async (e) => {
            e.stopPropagation();
            await playViaGlobalPlayer(songId, title, playBtn);
        });

        // 좋아요 토글 (✅ 음원 전체 방식: ❤ 고정 + liked 클래스 토글)
        const heartBtn = li.querySelector(".heart-btn");
        const likeNumEl = li.querySelector(".like-number");

        heartBtn.addEventListener("click", async (e) => {
            e.stopPropagation();

            if (!getToken()) {
                showLoginPopup();
                return;
            }

            try {
                const res = await authFetch(SONG_LIKE_API(songId), { method: "POST" });
                if (!res) return;

                const payload = await res.json();
                if (!payload?.success) return;

                const { liked, likeCount } = payload.data ?? {};
                heartBtn.classList.toggle("liked", liked === true);
                if (typeof likeCount === "number") likeNumEl.textContent = String(likeCount);
            } catch (err) {
                console.error("좋아요 토글 실패:", err);
            }
        });

        listEl.appendChild(li);

        hydrateRowWithDetail(li, songId, hasToken);
    });

    syncPlayButtons();
}

/* =========================
   Play (Global) ✅ /play 사용 + ✅ setPlayerQueue 사용
========================= */
async function playViaGlobalPlayer(songId, title, playBtn) {
    if (typeof window.setPlayerQueue !== "function" || typeof window.playSongFromPage !== "function") {
        alert("전역 플레이어가 아직 로드되지 않았습니다.");
        return;
    }

    const audioUrl = await fetchStreamingUrl(songId);
    if (!audioUrl) return;

    const tracks = buildChartTracksForCurrentPage();
    window.setPlayerQueue(tracks, songId, { loop: true, contextKey: `chart:top100:p${page}` });

    playBtn.dataset.audioUrl = audioUrl;

    const globalAudio = getGlobalAudioEl();
    const same = isSameTrack(globalAudio, audioUrl);

    if (currentPlayBtn && currentPlayBtn !== playBtn) {
        setPlayBtnState(currentPlayBtn, false);
    }
    currentSongId = songId;
    currentPlayBtn = playBtn;

    try {
        await window.playSongFromPage(audioUrl, title, songId);
        if (!same) setPlayBtnState(playBtn, true);
        else syncPlayButtons();
    } catch (e) {
        console.error(e);
        setPlayBtnState(playBtn, false);
        currentSongId = null;
        currentPlayBtn = null;
        alert("재생에 실패했습니다.");
    }
}

async function fetchStreamingUrl(songId) {
    try {
        const res = await request(SONG_PLAY_API(songId));
        if (!res) return null;

        let payload = null;
        try {
            payload = await res.json();
        } catch {
            payload = null;
        }

        if (!res.ok || payload?.success === false) {
            alert(payload?.message || "재생 가능한 음원이 없습니다.");
            return null;
        }

        const raw = payload?.data?.streamingUrl ?? null;
        const url = resolveAudioUrl(raw);

        if (!url) {
            alert("재생 가능한 음원 주소가 없습니다.");
            return null;
        }

        return url;
    } catch (e) {
        console.error(e);
        alert("재생 정보를 불러오지 못했습니다.");
        return null;
    }
}

/* =========================
   UI Sync
========================= */
function syncPlayButtons() {
    const globalAudio = getGlobalAudioEl();

    document.querySelectorAll(".chart-item").forEach((li) => {
        const btn = li.querySelector(".track-play");
        if (!btn) return;

        const url = btn.dataset.audioUrl || null;

        if (!url || !globalAudio) {
            setPlayBtnState(btn, false);
            return;
        }

        const same = isSameTrack(globalAudio, url);
        const isPlaying = same && !globalAudio.paused;

        setPlayBtnState(btn, isPlaying);
    });
}

/* =========================
   Detail hydration (albumImage + like)
========================= */
async function hydrateRowWithDetail(li, songId, hasToken) {
    const detail = await fetchSongDetail(songId);
    if (!detail) return;

    const img = li.querySelector(".thumb img");
    img.src = detail.albumImage || "/images/default.png";

    const likeNumEl = li.querySelector(".like-number");
    const heartBtn = li.querySelector(".heart-btn");

    if (likeNumEl) likeNumEl.textContent = String(detail.likeCount ?? 0);

    if (heartBtn) {
        // 비로그인: disabled 고정
        heartBtn.classList.toggle("disabled", !hasToken);

        // 로그인: liked 반영
        if (hasToken) {
            heartBtn.classList.toggle("liked", !!detail.liked);
        } else {
            heartBtn.classList.remove("liked");
        }
    }
}

async function fetchSongDetail(songId) {
    try {
        const res = await request(SONG_DETAIL_API(songId));
        if (!res) return null;

        const payload = await res.json();
        if (!payload?.success) return null;
        return payload.data;
    } catch (e) {
        console.error(e);
        return null;
    }
}

/* =========================
   Pagination helpers
========================= */
function totalPages() {
    return Math.max(1, Math.ceil(top.length / PAGE_SIZE));
}

function showError(msg) {
    errorBox.classList.remove("hidden");
    errorBox.textContent = msg;
}
function hideError() {
    errorBox.classList.add("hidden");
    errorBox.textContent = "";
}