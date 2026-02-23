import { authFetch, getToken } from "./auth.js";
import { resolveAudioUrl } from "/js/player-hls.js";

const songList = document.getElementById("songList");
const loadingEl = document.getElementById("loading");
const endMessage = document.getElementById("endMessage");
const sentinel = document.getElementById("sentinel");

let lastLikeId = null;
let hasNext = true;
let isLoading = false;

const SONG_PLAY_API = (id) => `/api/songs/${id}/play`;

/* =========================
   ✅ Login redirect (팝업 X)
========================= */
function redirectToLogin() {
    location.href = "/login";
}

/* =========================
   ✅ Auth request helper
========================= */
async function authedRequest(url, options = {}) {
    if (!getToken()) {
        redirectToLogin();
        return null;
    }
    const res = await authFetch(url, options);
    if (!res) return null;
    return res;
}

/* =========================
   Album image helper
========================= */
function resolveImageUrl(imagePath) {
    if (!imagePath) return "/images/default.png";
    if (String(imagePath).startsWith("http://") || String(imagePath).startsWith("https://")) return imagePath;
    if (String(imagePath).startsWith("/")) return imagePath;
    return `/${imagePath}`;
}

/* =========================
   Global Player helpers
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

/* ✅ 이 페이지에서 마지막으로 누른 재생 버튼 */
let currentPlayBtn = null;

/* =========================
   ✅ "현재 DOM(지금 렌더된 목록)" 기준 큐 생성/등록
   (이전/다음 버튼용: 기존 로직 유지)
========================= */
function buildQueueFromDom() {
    const rows = Array.from(document.querySelectorAll(".liked-row[data-id]"));
    return rows
        .map((row) => {
            const songId = Number(row.dataset.id);
            const title = row.querySelector(".song-title")?.textContent?.trim()
                || row.querySelector(".song-text")?.textContent?.trim()
                || "Unknown";
            if (!Number.isFinite(songId)) return null;
            return { songId, title };
        })
        .filter(Boolean);
}

function setGlobalQueueFromThisPage(startSongId) {
    const tracks = buildQueueFromDom();
    if (!tracks.length) return;

    if (typeof window.setPlayerQueue === "function") {
        window.setPlayerQueue(tracks, Number(startSongId), {
            loop: true,
            contextKey: "likes:songs",
        });
    }
}

/* 전역 오디오 이벤트로 버튼 상태 싱크 */
function wireGlobalAudioSync() {
    const globalAudio = getGlobalAudioEl();
    if (!globalAudio) return;

    const sync = () => {
        document.querySelectorAll(".liked-row[data-id]").forEach((row) => {
            const btn = row.querySelector(".play-btn");
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

        if (currentPlayBtn) {
            const url = currentPlayBtn.dataset.audioUrl || null;
            const same = url ? isSameTrack(globalAudio, url) : false;
            if (!same) currentPlayBtn = null;
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
function decodeHtmlEntities(str) {
    if (str == null) return "";
    const txt = document.createElement("textarea");
    txt.innerHTML = String(str);
    return txt.value;
}

async function getStreamingUrl(songId) {
    const res = await authedRequest(SONG_PLAY_API(songId), { method: "GET" });
    if (!res) return null;

    const payload = await res.json().catch(() => null);
    if (!res.ok || payload?.success === false) {
        alert(payload?.message || "재생에 실패했습니다.");
        return null;
    }

    const raw = payload?.data?.streamingUrl ?? null;
    return resolveAudioUrl(raw);
}

/* =========================
   Init
========================= */
init();

async function init() {
    // ✅ 페이지 진입 자체를 비로그인 차단 (다른 좋아요 페이지 UX)
    if (!getToken()) {
        redirectToLogin();
        return;
    }

    wireGlobalAudioSync();
    await load();
    setupInfiniteScroll();
}

/* =========================
   Load (Keyset)
========================= */
async function load() {
    if (!hasNext || isLoading) return;

    if (!getToken()) {
        redirectToLogin();
        hasNext = false;
        return;
    }

    isLoading = true;
    loadingEl?.classList.remove("hidden");

    let url = "/api/users/likes/songs";
    if (lastLikeId !== null) url += `?lastLikeId=${lastLikeId}`;

    try {
        const res = await authedRequest(url, { method: "GET" });
        if (!res) return;

        const result = await res.json().catch(() => null);
        if (!result?.success) return;

        const page = result.data;
        render(page.content);

        hasNext = page.hasNext === true;

        if (page.nextCursor && page.nextCursor.lastId != null) {
            lastLikeId = page.nextCursor.lastId;
        }

        if (!hasNext) {
            endMessage?.classList.remove("hidden");
            observer && observer.disconnect();
        }

        syncPlayButtons();
    } catch (e) {
        console.error("로드 실패:", e);
    } finally {
        loadingEl?.classList.add("hidden");
        isLoading = false;
    }
}

/* =========================
   Render
   ✅ 2줄 구조: 제목 / 아티스트명 · 앨범명
========================= */
function render(list) {
    (list || []).forEach((song) => {
        const row = document.createElement("div");
        row.className = "liked-row";
        row.dataset.id = String(song.songId);

        const title = decodeHtmlEntities(song.name ?? "-");
        const coverUrl = resolveImageUrl(song.albumImage);

        // ✅ 응답 DTO 필드명에 맞춰 사용 (없으면 안전 처리)
        const artistName =
            decodeHtmlEntities(song.artistName ?? song.artist ?? song?.artistList?.[0]?.artistName ?? "-");
        const albumName =
            decodeHtmlEntities(song.albumName ?? song.album ?? song?.albumName ?? "-");

        const meta = `${artistName} · ${albumName}`;

        row.innerHTML = `
      <div class="col title">
        <div class="song-main">
          <img class="album-thumb" src="${coverUrl}" alt="">
          <div class="song-text">
            <div class="song-title">${title}</div>
            <div class="song-meta">${meta}</div>
          </div>
        </div>
      </div>

      <div class="col actions">
        <div class="action-group">
          <button class="play-btn"
                  type="button"
                  aria-label="재생"
                  data-id="${song.songId}">▶</button>

          <span class="like-number">${song.likeCount ?? 0}</span>

          <button class="heart-btn liked"
                  type="button"
                  aria-label="좋아요"
                  data-id="${song.songId}">❤</button>
        </div>
      </div>
    `;

        songList.appendChild(row);
    });
}

/* 현재 전역 재생 상태를 버튼들에 반영 */
function syncPlayButtons() {
    const globalAudio = getGlobalAudioEl();
    if (!globalAudio) return;

    document.querySelectorAll(".liked-row[data-id]").forEach((row) => {
        const btn = row.querySelector(".play-btn");
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
}

/* =========================
   Infinite Scroll
========================= */
let observer = null;

function setupInfiniteScroll() {
    observer = new IntersectionObserver(
        async (entries) => {
            if (!entries[0].isIntersecting) return;
            await load();
        },
        { root: null, rootMargin: "300px", threshold: 0 }
    );

    if (sentinel) observer.observe(sentinel);
}

/* =========================
   Click Delegation
========================= */
songList.addEventListener("click", async (e) => {
    /* 1) 재생 버튼 */
    const playBtn = e.target.closest(".play-btn");
    if (playBtn) {
        e.stopPropagation();

        if (!getToken()) {
            redirectToLogin();
            return;
        }

        if (typeof window.playSongFromPage !== "function") {
            alert("전역 플레이어가 아직 로드되지 않았습니다.");
            return;
        }

        const songId = Number(playBtn.dataset.id);
        const row = playBtn.closest(".liked-row");

        const title =
            row?.querySelector(".song-title")?.textContent?.trim()
            || row?.querySelector(".song-text")?.textContent?.trim()
            || "Unknown";

        const url = await getStreamingUrl(songId);
        if (!url) return;

        playBtn.dataset.audioUrl = url;

        // ✅ 이전/다음 버튼용 큐 등록 (기존 로직 유지)
        setGlobalQueueFromThisPage(songId);

        if (currentPlayBtn && currentPlayBtn !== playBtn) {
            setPlayBtnState(currentPlayBtn, false);
        }
        currentPlayBtn = playBtn;

        try {
            await window.playSongFromPage(url, title, songId);

            const globalAudio = getGlobalAudioEl();
            const same = isSameTrack(globalAudio, url);
            if (!same) setPlayBtnState(playBtn, true);
            else syncPlayButtons();
        } catch (err) {
            console.error(err);
            setPlayBtnState(playBtn, false);
            alert("재생에 실패했습니다.");
        }

        return;
    }

    /* 2) 좋아요 토글 */
    const heartBtn = e.target.closest(".heart-btn");
    if (heartBtn) {
        e.stopPropagation();

        if (!getToken()) {
            redirectToLogin();
            return;
        }

        const songId = heartBtn.dataset.id;

        try {
            const res = await authedRequest(`/api/songs/${songId}/likes`, { method: "POST" });
            if (!res) return;

            const result = await res.json().catch(() => null);
            if (!result?.success) return;

            // ✅ 좋아요 목록 페이지니까 성공하면 무조건 제거
            const row = heartBtn.closest(".liked-row");
            row?.remove();

        } catch (e2) {
            console.error("좋아요 토글 실패:", e2);
        }

        return;
    }

    /* 3) row 클릭 → 상세 */
    const row = e.target.closest(".liked-row");
    if (!row) return;

    if (!getToken()) {
        redirectToLogin();
        return;
    }

    const songId = row.dataset.id;
    if (!songId) return;

    location.href = `/songs/${songId}/page`;
});