import { authFetch, getToken } from "./auth.js";

const listEl = document.getElementById("chartList");
const errorBox = document.getElementById("errorBox");
const prevBtn = document.getElementById("prevBtn");
const nextBtn = document.getElementById("nextBtn");
const pageInfo = document.getElementById("pageInfo");

const TOP100_API = "/api/songs/ranking/Top100";
const SONG_DETAIL_API = (songId) => `/api/songs/${songId}`;
const SONG_PAGE_URL = (songId) => `/songs/${songId}/page`;

const PAGE_SIZE = 10;

let top = [];
let page = 0;

// ✅ 차트에서 “현재 선택된 곡” 표시용
let currentSongId = null;
let currentPlayBtn = null;

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
        // 현재 페이지에 보이는 버튼들만 상태 반영
        document.querySelectorAll(".chart-item").forEach((li) => {
            const btn = li.querySelector(".track-play");
            if (!btn) return;

            const url = btn.dataset.audioUrl || null;
            const same = url ? isSameTrack(globalAudio, url) : false;
            const isPlaying = same && !globalAudio.paused;

            setPlayBtnState(btn, isPlaying);
        });

        // currentSongId/currentPlayBtn 정리(다른 곡으로 넘어간 경우)
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
}

/* =========================
   Utils
========================= */
function normalizeAudioUrl(audioPath) {
    if (!audioPath) return null;
    const s = String(audioPath);
    if (s.startsWith("http://") || s.startsWith("https://")) return s;
    if (s.startsWith("/")) return s;
    return `/${s}`;
}

function escapeHtml(str) {
    return String(str)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function decodeHtmlEntities(str) {
    const txt = document.createElement("textarea");
    txt.innerHTML = String(str);
    return txt.value;
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

    const start = page * PAGE_SIZE;
    const items = top.slice(start, start + PAGE_SIZE);

    pageInfo.textContent = `${page + 1} / ${totalPages()}`;
    prevBtn.disabled = page === 0;
    nextBtn.disabled = page === totalPages() - 1;

    items.forEach((row, idx) => {
        const rank = start + idx + 1;

        const songId = row.id; // Top100 응답이 id
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

          <button class="track-play" type="button" aria-label="재생">▶</button>
        `;

        // 행 클릭 -> 단건조회 (/page)
        li.addEventListener("click", (e) => {
            if (e.target.closest(".track-play")) return;
            location.href = SONG_PAGE_URL(songId);
        });

        // 재생/일시정지 (전역 플레이어)
        const playBtn = li.querySelector(".track-play");
        playBtn.addEventListener("click", async (e) => {
            e.stopPropagation();
            await playViaGlobalPlayer(songId, title, playBtn);
        });

        listEl.appendChild(li);

        // 상세로 앨범 이미지 보강
        hydrateRowWithDetail(li, songId);
    });

    // 렌더 후 상태 반영
    syncPlayButtons();
}

/* =========================
   Play (Global)
========================= */
async function playViaGlobalPlayer(songId, title, playBtn) {
    if (typeof window.playSongFromPage !== "function") {
        alert("전역 플레이어가 아직 로드되지 않았습니다.");
        return;
    }

    // ✅ audioUrl 확보
    const audioUrl = await fetchAudioUrl(songId);
    if (!audioUrl) return;

    // 버튼에 audioUrl 저장(전역 오디오와 같은 곡인지 비교용)
    playBtn.dataset.audioUrl = audioUrl;

    const globalAudio = getGlobalAudioEl();
    const same = isSameTrack(globalAudio, audioUrl);

    // 현재 선택 곡 갱신
    if (currentPlayBtn && currentPlayBtn !== playBtn) {
        setPlayBtnState(currentPlayBtn, false);
    }
    currentSongId = songId;
    currentPlayBtn = playBtn;

    try {
        await window.playSongFromPage(audioUrl, title, songId);

        // 호출 후 버튼 표시(전역 이벤트에서도 싱크됨)
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

async function fetchAudioUrl(songId) {
    try {
        const res = getToken()
            ? await authFetch(SONG_DETAIL_API(songId), { method: "GET" })
            : await fetch(SONG_DETAIL_API(songId));

        if (!res) return null;

        const payload = await res.json();
        const raw = payload?.data?.audio;
        const url = normalizeAudioUrl(raw);

        if (!res.ok || payload?.success === false || !url) {
            alert(payload?.message || "재생 가능한 음원 주소가 없습니다.");
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

        // url 없으면(아직 클릭 안한 버튼) 그냥 기본 ▶
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
   Detail hydration (albumImage)
========================= */
async function hydrateRowWithDetail(li, songId) {
    const detail = await fetchSongDetail(songId);
    if (!detail) return;

    const img = li.querySelector(".thumb img");
    img.src = detail.albumImage || "/images/default.png";
}

async function fetchSongDetail(songId) {
    try {
        const res = getToken()
            ? await authFetch(SONG_DETAIL_API(songId), { method: "GET" })
            : await fetch(SONG_DETAIL_API(songId));

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
