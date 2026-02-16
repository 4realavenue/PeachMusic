import { authFetch, getToken } from "./auth.js";

const listEl = document.getElementById("chartList");
const errorBox = document.getElementById("errorBox");
const prevBtn = document.getElementById("prevBtn");
const nextBtn = document.getElementById("nextBtn");
const pageInfo = document.getElementById("pageInfo");
const audio = document.getElementById("chartAudio");

const TOP100_API = "/api/songs/ranking/Top100";
const SONG_DETAIL_API = (songId) => `/api/songs/${songId}`;
const SONG_PAGE_URL = (songId) => `/songs/${songId}/page`;

const PAGE_SIZE = 10;

let top = [];
let page = 0;
let currentSongId = null; // ✅ 지금 재생중인 곡

document.addEventListener("DOMContentLoaded", async () => {
    await loadTop100();

    prevBtn.addEventListener("click", () => {
        if (page === 0) return;
        page--;
        render();
        window.scrollTo({ top: 0, behavior: "smooth" });
    });

    nextBtn.addEventListener("click", () => {
        if (page >= totalPages() - 1) return;
        page++;
        render();
        window.scrollTo({ top: 0, behavior: "smooth" });
    });

    // ✅ 오디오 끝나면 버튼 상태 복구
    audio.addEventListener("ended", () => {
        currentSongId = null;
        syncPlayButtons();
    });

    audio.addEventListener("pause", () => syncPlayButtons());
    audio.addEventListener("play", () => syncPlayButtons());
});

async function loadTop100() {
    hideError();
    listEl.innerHTML = "";

    try {
        const res = await fetch(TOP100_API);
        const payload = await res.json();

        if (!res.ok || payload?.success === false) {
            showError(payload?.message ?? "차트 조회 실패");
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
        showError("서버 오류");
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

        const songId = row.id; // ✅ Top100 응답이 id
        const title = decodeHtmlEntities(row.title ?? "-");

        const li = document.createElement("li");
        li.className = "chart-item";
        li.dataset.songId = songId;

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

        // ✅ 행 클릭 -> 단건조회 (/page)
        li.addEventListener("click", (e) => {
            if (e.target.closest(".track-play")) return;
            location.href = SONG_PAGE_URL(songId);
        });

        // ✅ 재생/일시정지 토글
        const playBtn = li.querySelector(".track-play");
        playBtn.addEventListener("click", async (e) => {
            e.stopPropagation();
            await togglePlay(songId);
        });

        listEl.appendChild(li);

        // ✅ 상세로 앨범 이미지 보강
        hydrateRowWithDetail(li, songId);
    });

    // ✅ 렌더 후 현재 재생 상태 반영
    syncPlayButtons();
}

async function togglePlay(songId) {
    // 같은 곡 누르면 토글
    if (currentSongId === songId) {
        if (audio.paused) {
            await audio.play();
        } else {
            audio.pause();
        }
        return;
    }

    const res = await fetch(`/api/songs/${songId}/play`);
    const payload = await res.json();
    const url = payload?.data?.streamingUrl;

    if (!res.ok || payload?.success === false || !url) {
        alert(payload?.message || "재생 가능한 음원 주소가 없습니다.");
        return;
    }

    currentSongId = songId;
    audio.src = url;

    try {
        await audio.play();
    } catch (e) {
        console.error(e);
        alert("재생에 실패했습니다.");
    }
}

function syncPlayButtons() {
    // 현재 페이지에 보이는 버튼들만 상태 반영
    document.querySelectorAll(".chart-item").forEach((li) => {
        const songId = Number(li.dataset.songId);
        const btn = li.querySelector(".track-play");

        const isCurrent = currentSongId === songId;
        const isPlaying = isCurrent && !audio.paused;

        btn.classList.toggle("playing", isPlaying);
        btn.textContent = isPlaying ? "❚❚" : "▶";
        btn.setAttribute("aria-label", isPlaying ? "일시정지" : "재생");
    });
}

async function hydrateRowWithDetail(li, songId) {
    const detail = await fetchSongDetail(songId);
    if (!detail) return;

    const img = li.querySelector(".thumb img");
    img.src = detail.albumImage || "/images/default.png";
}

async function fetchSongDetail(songId) {
    try {
        // 토큰 있으면 authFetch로 liked 같은 값까지 맞출 수 있는데,
        // 지금은 차트에서 좋아요 제거했으니 fetch만 써도 됨.
        const res = getToken()
            ? await authFetch(SONG_DETAIL_API(songId), { method: "GET" })
            : await fetch(SONG_DETAIL_API(songId));

        if (!res) return null;

        const payload = await res.json();
        if (!payload.success) return null;
        return payload.data;

    } catch (e) {
        console.error(e);
        return null;
    }
}

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