import { authFetch, getToken } from "./auth.js";

const hasToken = !!getToken();

const songList = document.getElementById("songList");
const loadingEl = document.getElementById("loading");
const endMessage = document.getElementById("endMessage");
const sentinel = document.getElementById("sentinel");

let lastLikeId = null;
let hasNext = true;
let isLoading = false;

const SONG_PLAY_API = (id) => `/api/songs/${id}/play`;

async function getStreamingUrl(songId) {
    const res = await authFetch(SONG_PLAY_API(songId), { method: "GET" });
    if (!res) return null;

    const payload = await res.json();
    if (!res.ok || payload?.success === false) return null;

    return payload.data?.streamingUrl ?? null;
}

/* ✅ 프리뷰 재생(한 페이지에 오디오 1개) */
const previewAudio = new Audio();
previewAudio.preload = "metadata";

let currentPlayingSongId = null;
let currentPlayBtn = null;

function setPlayBtnState(btn, isPlaying) {
    if (!btn) return;
    btn.classList.toggle("playing", isPlaying);
    btn.textContent = isPlaying ? "❚❚" : "▶";
}

/* ✅ 재생 종료/정지 시 버튼 원복 */
previewAudio.addEventListener("ended", () => {
    if (currentPlayBtn) setPlayBtnState(currentPlayBtn, false);
    currentPlayingSongId = null;
    currentPlayBtn = null;
});
previewAudio.addEventListener("pause", () => {
    if (currentPlayBtn) setPlayBtnState(currentPlayBtn, false);
});
previewAudio.addEventListener("play", () => {
    if (currentPlayBtn) setPlayBtnState(currentPlayBtn, true);
});

function decodeHtmlEntities(str) {
    if (str == null) return "";
    const txt = document.createElement("textarea");
    txt.innerHTML = String(str);
    return txt.value;
}

init();

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

    let url = "/api/users/likes/songs";
    if (lastLikeId !== null) url += `?lastLikeId=${lastLikeId}`;

    try {
        const res = await authFetch(url);
        const result = await res.json();
        if (!result.success) return;

        const page = result.data;

        render(page.content);

        hasNext = page.hasNext;

        if (page.nextCursor && page.nextCursor.lastId != null) {
            lastLikeId = page.nextCursor.lastId;
        }

        if (!hasNext) {
            endMessage.classList.remove("hidden");
            observer && observer.disconnect();
        }
    } catch (e) {
        console.error("로드 실패:", e);
    } finally {
        loadingEl.classList.add("hidden");
        isLoading = false;
    }
}

/* =========================
   렌더링
========================= */
function render(list) {
    list.forEach(song => {
        const row = document.createElement("div");
        row.className = "liked-row";
        row.dataset.id = song.songId; // ✅ row 클릭 시 상세 이동에 사용

        const title = decodeHtmlEntities(song.name ?? "-");

        row.innerHTML = `
            <div class="col play">
                <button class="play-btn"
                        type="button"
                        aria-label="재생"
                        data-id="${song.songId}"
                        data-audio="${song.audio ?? ""}">▶</button>
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

/* =========================
   무한 스크롤
========================= */
let observer = null;

function setupInfiniteScroll() {
    observer = new IntersectionObserver(async (entries) => {
        if (!entries[0].isIntersecting) return;
        await load();
    }, {
        root: null,
        rootMargin: "300px",
        threshold: 0
    });

    observer.observe(sentinel);
}

/* =========================
   클릭 이벤트 (위임)
   1) 재생 버튼
   2) 하트 토글
   3) row 클릭 → 음원 단건조회(/page)
========================= */
songList.addEventListener("click", async (e) => {

    /* ✅ 1) 재생 버튼 */
    const playBtn = e.target.closest(".play-btn");
    if (playBtn) {
        e.stopPropagation();

        const songId = playBtn.dataset.id;

        const audioUrl = await getStreamingUrl(songId);
        if (!audioUrl) {
            alert("재생 가능한 음원 주소가 없습니다.");
            return;
        }

        // 같은 곡이면 토글
        if (currentPlayingSongId === songId) {
            if (previewAudio.paused) await previewAudio.play();
            else previewAudio.pause();
            return;
        }

        // 다른 곡 재생: 이전 버튼 초기화
        if (currentPlayBtn) setPlayBtnState(currentPlayBtn, false);

        currentPlayingSongId = songId;
        currentPlayBtn = playBtn;

        previewAudio.src = audioUrl;
        await previewAudio.play();
        return;
    }

    /* ✅ 2) 좋아요 토글 */
    const heartBtn = e.target.closest(".heart-btn");
    if (heartBtn) {
        const songId = heartBtn.dataset.id;

        try {
            const res = await authFetch(`/api/songs/${songId}/likes`, { method: "POST" });
            if (!res) return;

            const result = await res.json();
            if (!result.success) return;

            const { liked, likeCount } = result.data;

            heartBtn.classList.toggle("liked", liked);
            heartBtn.textContent = liked ? "❤" : "🤍";

            const likeNumber = heartBtn.closest(".liked-row").querySelector(".like-number");
            likeNumber.textContent = likeCount;

        } catch (e2) {
            console.error("좋아요 토글 실패:", e2);
        }
        return;
    }

    /* ✅ 3) 나머지 영역 클릭 → 음원 단건조회(/page) */
    const row = e.target.closest(".liked-row");
    if (!row) return;

    const songId = row.dataset.id;
    if (!songId) return;

    location.href = `/songs/${songId}/page`;
});