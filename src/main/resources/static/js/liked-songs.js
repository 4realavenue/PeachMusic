import { authFetch } from "./auth.js";

const songList = document.getElementById("songList");
const loadingEl = document.getElementById("loading");
const endMessage = document.getElementById("endMessage");
const sentinel = document.getElementById("sentinel");

let lastLikeId = null;
let hasNext = true;
let isLoading = false;

const SONG_PLAY_API = (id) => `/api/songs/${id}/play`;

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
    const res = await authFetch(SONG_PLAY_API(songId), { method: "GET" });
    if (!res) return null;

    const payload = await res.json().catch(() => null);
    if (!res.ok || payload?.success === false) {
        alert(payload?.message || "재생에 실패했습니다.");
        return null;
    }

    return payload?.data?.streamingUrl ?? null;
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

    isLoading = true;
    loadingEl?.classList.remove("hidden");

    let url = "/api/users/likes/songs";
    if (lastLikeId !== null) url += `?lastLikeId=${lastLikeId}`;

    try {
        const res = await authFetch(url);
        if (!res) return;

        const result = await res.json();
        if (!result?.success) return;

        const page = result.data;

        render(page.content);

        hasNext = page.hasNext;

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
    list.forEach((song) => {
        const row = document.createElement("div");
        row.className = "liked-row";
        row.dataset.id = String(song.songId);

        const title = decodeHtmlEntities(song.name ?? "-");

        // ✅ 재생 → 숫자 → 하트
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

    observer.observe(sentinel);
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

        if (typeof window.playSongFromPage !== "function") {
            alert("전역 플레이어가 아직 로드되지 않았습니다.");
            return;
        }

        const songId = playBtn.dataset.id;
        const row = playBtn.closest(".liked-row");
        const title = row?.querySelector(".col.title")?.textContent?.trim() || "Unknown";

        // ✅ streamingUrl 확보
        const url = await getStreamingUrl(songId);
        if (!url) return;

        // 버튼에 url 저장(전역 오디오와 같은 곡인지 비교용)
        playBtn.dataset.audioUrl = url;

        // 다른 버튼 눌렀으면 이전 버튼 원복(전역 이벤트로도 싱크됨)
        if (currentPlayBtn && currentPlayBtn !== playBtn) {
            setPlayBtnState(currentPlayBtn, false);
        }
        currentPlayBtn = playBtn;

        try {
            await window.playSongFromPage(url, title, Number(songId));

            // 호출 직후 UI 반영(전역 이벤트에서도 싱크됨)
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
        const songId = heartBtn.dataset.id;

        try {
            const res = await authFetch(`/api/songs/${songId}/likes`, { method: "POST" });
            if (!res) return;

            const result = await res.json();
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