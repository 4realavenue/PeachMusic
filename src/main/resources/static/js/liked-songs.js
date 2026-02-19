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
   Auth/UI helpers
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

/**
 * ✅ 토큰 만료/로그아웃 방어 포함 request
 * - 토큰 있으면 authFetch 사용 (authFetch가 401 처리 후 null 리턴할 수 있음)
 * - 토큰 없으면 null 반환 + 팝업
 */
async function authedRequest(url, options = {}) {
    if (!getToken()) {
        showLoginPopup();
        return null;
    }
    const res = await authFetch(url, options);
    // authFetch가 401 처리(토큰 제거/리다이렉트 등)하면 res가 null일 수 있음
    if (!res) return null;
    return res;
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
========================= */
function buildQueueFromDom() {
    const rows = Array.from(document.querySelectorAll(".liked-row[data-id]"));
    return rows
        .map((row) => {
            const songId = Number(row.dataset.id);
            const title = row.querySelector(".col.title")?.textContent?.trim() || "Unknown";
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

        // currentPlayBtn이 가리키는 곡이 전역에서 다른 곡으로 바뀌면 참조 해제
        if (currentPlayBtn) {
            const url = currentPlayBtn.dataset.audioUrl || null;
            const same = url ? isSameTrack(globalAudio, url) : false;
            if (!same) currentPlayBtn = null;
        }
    };

    globalAudio.addEventListener("play", sync);
    globalAudio.addEventListener("pause", sync);
    globalAudio.addEventListener("ended", sync);

    // ✅ 최초 1회 싱크
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

/**
 * ✅ /play 호출 (토큰 만료 방어 포함)
 * - 401/토큰만료 등으로 authFetch가 null이면 null 반환
 */
async function getStreamingUrl(songId) {
    const res = await authedRequest(SONG_PLAY_API(songId), { method: "GET" });
    if (!res) return null;

    const payload = await res.json().catch(() => null);
    if (!res.ok || payload?.success === false) {
        // 여기서 401은 보통 authFetch가 처리하니, 나머지만 경고
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
    wireGlobalAudioSync();
    await load();
    setupInfiniteScroll();
}

/* =========================
   Load (Keyset)
========================= */
async function load() {
    if (!hasNext || isLoading) return;

    // ✅ 토큰 없으면 더 로드하지 않음
    if (!getToken()) {
        showLoginPopup();
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

        // 렌더 후 전역 상태 반영
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
========================= */
function render(list) {
    (list || []).forEach((song) => {
        const row = document.createElement("div");
        row.className = "liked-row";
        row.dataset.id = String(song.songId);

        const title = decodeHtmlEntities(song.name ?? "-");

        row.innerHTML = `
      <div class="col play">
        <button class="play-btn"
                type="button"
                aria-label="재생"
                data-id="${song.songId}">▶</button>
      </div>

      <div class="col title">${title}</div>

      <div class="col like-count">
        <span class="like-number">${song.likeCount ?? 0}</span>
      </div>

      <div class="col heart">
        <button class="heart-btn liked"
                type="button"
                aria-label="좋아요"
                data-id="${song.songId}">❤</button>
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
   1) 재생(전역)
   2) 하트 토글
   3) row 클릭 → 상세
========================= */
songList.addEventListener("click", async (e) => {
    /* 1) 재생 버튼 */
    const playBtn = e.target.closest(".play-btn");
    if (playBtn) {
        e.stopPropagation();

        if (!getToken()) {
            showLoginPopup();
            return;
        }

        if (typeof window.playSongFromPage !== "function") {
            alert("전역 플레이어가 아직 로드되지 않았습니다.");
            return;
        }

        const songId = Number(playBtn.dataset.id);
        const row = playBtn.closest(".liked-row");
        const title = row?.querySelector(".col.title")?.textContent?.trim() || "Unknown";

        const url = await getStreamingUrl(songId);
        if (!url) return;

        // ✅ 싱크/비교용 url 저장
        playBtn.dataset.audioUrl = url;

        // ✅ 현재 DOM 기준 큐 등록
        setGlobalQueueFromThisPage(songId);

        // 다른 버튼 눌렀으면 이전 버튼 원복
        if (currentPlayBtn && currentPlayBtn !== playBtn) {
            setPlayBtnState(currentPlayBtn, false);
        }
        currentPlayBtn = playBtn;

        try {
            await window.playSongFromPage(url, title, songId);

            // 호출 직후 UI 반영
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
            showLoginPopup();
            return;
        }

        const songId = heartBtn.dataset.id;

        try {
            const res = await authedRequest(`/api/songs/${songId}/likes`, { method: "POST" });
            if (!res) return;

            const result = await res.json().catch(() => null);
            if (!result?.success) return;

            const { liked, likeCount } = result.data;

            heartBtn.classList.toggle("liked", liked);
            heartBtn.textContent = liked ? "❤" : "🤍";

            const likeNumber = heartBtn.closest(".liked-row")?.querySelector(".like-number");
            if (likeNumber) likeNumber.textContent = likeCount ?? 0;
        } catch (e2) {
            console.error("좋아요 토글 실패:", e2);
        }
        return;
    }

    /* 3) row 클릭 → 상세 */
    const row = e.target.closest(".liked-row");
    if (!row) return;

    const songId = row.dataset.id;
    if (!songId) return;

    location.href = `/songs/${songId}/page`;
});