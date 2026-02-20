import { authFetch, getToken, removeToken } from "./auth.js";
import { resolveAudioUrl } from "/js/player-hls.js";

const popup = document.getElementById("loginPopup");

let dataCache = null;

const el = {
    albumImage: document.getElementById("albumImage"),
    songName: document.getElementById("songName"),
    artistName: document.getElementById("artistName"),
    albumLink: document.getElementById("albumLink"),
    position: document.getElementById("position"),
    genreChips: document.getElementById("genreChips"),

    likeCount: document.getElementById("likeCount"),
    heartBtn: document.getElementById("heartBtn"),

    addToPlaylistBtn: document.getElementById("addToPlaylistBtn"),

    duration: document.getElementById("duration"),

    lang: document.getElementById("lang"),
    speed: document.getElementById("speed"),
    vocalInstrumental: document.getElementById("vocalInstrumental"),

    instrumentals: document.getElementById("instrumentals"),
    vartags: document.getElementById("vartags"),
    licenseLink: document.getElementById("licenseLink"),

    detailPlayBtn: document.getElementById("detailPlayBtn"),

    playlistModal: document.getElementById("playlistModal"),
    playlistList: document.getElementById("playlistList"),
    playlistModalClose: document.getElementById("playlistModalClose"),
    playlistModalCancel: document.getElementById("playlistModalCancel"),
};

const SONG_DETAIL_API = (id) => `/api/songs/${id}`;
const SONG_LIKE_API = (id) => `/api/songs/${id}/likes`;
const SONG_PLAY_API = (id) => `/api/songs/${id}/play`;

const PLAYLIST_LIST_API = "/api/playlists";
const PLAYLIST_ADD_API = (playlistId) => `/api/playlists/${playlistId}/songs`;

/* =========================
   Auth / JSON helpers
========================= */
function showLoginPopup() {
    if (!popup) return;
    popup.classList.remove("hidden");
    popup.classList.add("show");
    setTimeout(() => {
        popup.classList.remove("show");
        popup.classList.add("hidden");
    }, 2000);
}

function handleUnauthorized(message) {
    alert(message || "로그인이 필요합니다.");
    try {
        removeToken?.();
    } catch {}
    location.href = "/login";
}

async function readJsonSafe(res) {
    try {
        return await res.json();
    } catch {
        return null;
    }
}

/**
 * ✅ 로그인 필요한 API 전용(JSON)
 * - authFetch 사용
 * - 401이면 토큰 제거 + /login
 */
async function authFetchJson(url, options = {}, fallback = "요청 실패") {
    const res = await authFetch(url, options);
    if (!res) return { ok: false, data: null, res: null }; // authFetch 내부에서 처리했을 수도

    const data = await readJsonSafe(res);

    if (res.status === 401) {
        handleUnauthorized(data?.message);
        return { ok: false, data, res };
    }

    if (!res.ok) {
        alert(data?.message || fallback);
        return { ok: false, data, res };
    }

    if (data && data.success === false) {
        alert(data?.message || fallback);
        return { ok: false, data, res };
    }

    return { ok: true, data, res };
}

/* =========================
   Utils
========================= */
function formatDuration(seconds) {
    if (seconds == null) return "-";
    const n = Number(seconds);
    if (!Number.isFinite(n) || n < 0) return "-";
    const min = Math.floor(n / 60);
    const sec = Math.floor(n % 60);
    return `${min}:${String(sec).padStart(2, "0")}`;
}

function pickCaseInsensitive(obj, key) {
    if (!obj) return undefined;
    const target = String(key).toLowerCase();
    for (const k of Object.keys(obj)) {
        if (String(k).toLowerCase() === target) return obj[k];
    }
    return undefined;
}

function getDurationValue(data) {
    return (
        pickCaseInsensitive(data, "duration") ??
        pickCaseInsensitive(data, "trackDuration") ??
        pickCaseInsensitive(data, "durationSec") ??
        pickCaseInsensitive(data, "length")
    );
}

function getVocalInstrumentalValue(data) {
    return pickCaseInsensitive(data, "vocalInstrumental") ?? pickCaseInsensitive(data, "vocalinstrumental");
}

function resolveDurationEl() {
    return (
        document.getElementById("durationValue") ||
        document.getElementById("durationText") ||
        document.getElementById("songDuration") ||
        document.getElementById("duration") ||
        findValueCellByLabelText("duration")
    );
}

function findValueCellByLabelText(labelText) {
    const rows = Array.from(document.querySelectorAll(".song-info-row, .info-row, .row, li, div"));
    const lower = String(labelText).toLowerCase();

    for (const row of rows) {
        const text = (row.textContent || "").trim().toLowerCase();
        if (!text) continue;
        if (!text.includes(lower)) continue;

        const candidates = row.querySelectorAll("span, div, p, td");
        if (candidates.length >= 2) return candidates[candidates.length - 1];
    }
    return null;
}

/* =========================
   Global Player Sync
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

function setPlayBtnState(isPlaying) {
    if (!el.detailPlayBtn) return;
    el.detailPlayBtn.classList.toggle("playing", isPlaying);
    el.detailPlayBtn.textContent = isPlaying ? "⏸" : "▶";
    el.detailPlayBtn.setAttribute("aria-label", isPlaying ? "일시정지" : "재생");
}

function wireGlobalAudioSync() {
    const globalAudio = getGlobalAudioEl();
    if (!globalAudio) return;

    const sync = () => {
        const url = dataCache?.audioUrl;
        if (!url) return setPlayBtnState(false);

        const same = isSameTrack(globalAudio, url);
        setPlayBtnState(same && !globalAudio.paused);
    };

    globalAudio.addEventListener("play", sync);
    globalAudio.addEventListener("pause", sync);
    globalAudio.addEventListener("ended", sync);
    sync();
}

/* =========================
   ✅ setPlayerQueue (단건 = 1곡 큐)
========================= */
function setQueueForThisSong(songId, title) {
    if (typeof window.setPlayerQueue !== "function") return;
    window.setPlayerQueue(
        [{ songId: Number(songId), title: String(title || "Unknown") }],
        Number(songId),
        { loop: true, contextKey: "song:detail" }
    );
}

/* =========================
   ✅ /play (비로그인 가능)
   - 토큰 있으면 authFetch, 없으면 fetch
========================= */
async function fetchStreamingUrl(songId) {
    const url = SONG_PLAY_API(songId);

    const res = getToken()
        ? await authFetch(url, { method: "GET" })
        : await fetch(url, { method: "GET" });

    if (!res) return null;

    const payload = await readJsonSafe(res);

    if (!res.ok || payload?.success === false) {
        alert(payload?.message || "재생에 실패했습니다.");
        return null;
    }

    const raw = payload?.data?.streamingUrl ?? null;
    const fixed = resolveAudioUrl(raw);
    if (!fixed) {
        alert("재생 가능한 음원 주소가 없습니다.");
        return null;
    }
    return fixed;
}

/* =========================
   Init
========================= */
document.addEventListener("DOMContentLoaded", async () => {
    el.duration = resolveDurationEl();

    await loadSongDetail();
    wireGlobalAudioSync();

    el.heartBtn?.addEventListener("click", async (e) => {
        e.preventDefault();
        if (!getToken()) return showLoginPopup();
        await toggleLike();
    });

    el.detailPlayBtn?.addEventListener("click", async () => {
        if (!songId) return;

        if (typeof window.playSongFromPage !== "function") {
            alert("전역 플레이어가 아직 로드되지 않았습니다.");
            return;
        }
        if (typeof window.setPlayerQueue !== "function") {
            alert("전역 플레이어 큐 기능이 아직 로드되지 않았습니다.");
            return;
        }

        const title = dataCache?.name ?? "Unknown";

        // ✅ 1) 큐 세팅(1곡)
        setQueueForThisSong(songId, title);

        // ✅ 2) /play로 url 받고 재생
        const playUrl = await fetchStreamingUrl(songId);
        if (!playUrl) return;

        dataCache = { ...(dataCache || {}), audioUrl: playUrl };

        try {
            await window.playSongFromPage(playUrl, title, Number(songId));
        } catch (e) {
            console.error(e);
            alert("재생에 실패했습니다.");
        }
    });

    el.addToPlaylistBtn?.addEventListener("click", async () => {
        if (!getToken()) return showLoginPopup();
        await openPlaylistModal();
    });

    wirePlaylistModalClose();
});

/* =========================
   Load & Render
========================= */
async function loadSongDetail() {
    const res = getToken()
        ? await authFetch(SONG_DETAIL_API(songId), { method: "GET" })
        : await fetch(SONG_DETAIL_API(songId), { method: "GET" });

    if (!res) return;

    const response = await readJsonSafe(res);
    if (!response?.success) return;

    render(response.data);
}

function render(data) {
    dataCache = { ...data, audioUrl: null };

    if (el.albumImage) el.albumImage.src = data.albumImage || "/images/default.png";

    if (el.albumLink) {
        el.albumLink.textContent = data.albumName ?? "-";
        el.albumLink.href = data.albumId ? `/albums/${data.albumId}/page` : "#";
    }

    if (el.songName) el.songName.textContent = data.name ?? "-";
    if (el.position) el.position.textContent = data.position ?? "-";
    if (el.artistName) el.artistName.textContent = data.artistName ?? "-";

    if (el.genreChips) {
        el.genreChips.innerHTML = "";
        (data.genreList ?? []).forEach((g) => {
            const chip = document.createElement("span");
            chip.className = "chip";
            chip.textContent = g;
            el.genreChips.appendChild(chip);
        });
    }

    if (el.likeCount) el.likeCount.textContent = data.likeCount ?? 0;
    el.heartBtn?.classList.toggle("liked", data.liked === true);

    if (!el.duration) el.duration = resolveDurationEl();
    if (el.duration) {
        const durationRaw = getDurationValue(data);
        el.duration.textContent = formatDuration(durationRaw);
    }

    if (el.lang) el.lang.textContent = data.lang ?? "-";
    if (el.speed) el.speed.textContent = data.speed ?? "-";
    if (el.vocalInstrumental) el.vocalInstrumental.textContent = getVocalInstrumentalValue(data) ?? "-";
    if (el.instrumentals) el.instrumentals.textContent = data.instrumentals ?? "-";
    if (el.vartags) el.vartags.textContent = data.vartags ?? "-";

    if (el.licenseLink) {
        const license = data.licenseCcurl ?? "-";
        el.licenseLink.textContent = license;
        el.licenseLink.href = license && license !== "-" ? license : "#";
    }
}

/* =========================
   Like (로그인 필요)
========================= */
async function toggleLike() {
    const { ok, data } = await authFetchJson(SONG_LIKE_API(songId), { method: "POST" }, "좋아요 처리 실패");
    if (!ok) return;

    const result = data?.data;
    el.heartBtn?.classList.toggle("liked", result?.liked === true);
    if (el.likeCount) el.likeCount.textContent = result?.likeCount ?? 0;
}

/* =========================
   Playlist Modal (로그인 필요)
========================= */
function wirePlaylistModalClose() {
    if (!el.playlistModal) return;

    el.playlistModalClose?.addEventListener("click", closePlaylistModal);
    el.playlistModalCancel?.addEventListener("click", closePlaylistModal);

    el.playlistModal.addEventListener("click", (e) => {
        if (e.target?.dataset?.close === "true") closePlaylistModal();
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !el.playlistModal.classList.contains("hidden")) closePlaylistModal();
    });
}

async function openPlaylistModal() {
    if (!el.playlistModal || !el.playlistList) return;

    el.playlistModal.classList.remove("hidden");
    el.playlistModal.setAttribute("aria-hidden", "false");
    el.playlistList.innerHTML = "불러오는 중...";

    const { ok, data } = await authFetchJson(PLAYLIST_LIST_API, { method: "GET" }, "플레이리스트 조회에 실패했습니다.");
    if (!ok) return;

    const playlists = data?.data ?? [];
    el.playlistList.innerHTML = "";

    if (playlists.length === 0) {
        el.playlistList.innerHTML = "플레이리스트가 없습니다.";
        return;
    }

    playlists.forEach((pl) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.textContent = pl.playlistName ?? "-";
        btn.className = "pl-item";
        btn.onclick = () => addSongToPlaylist(pl.playlistId);
        el.playlistList.appendChild(btn);
    });
}

function closePlaylistModal() {
    if (!el.playlistModal) return;
    el.playlistModal.classList.add("hidden");
    el.playlistModal.setAttribute("aria-hidden", "true");
}

async function addSongToPlaylist(playlistId) {
    const body = { songIdSet: [Number(songId)] };

    const { ok, data, res } = await authFetchJson(
        PLAYLIST_ADD_API(playlistId),
        { method: "POST", body: JSON.stringify(body) },
        "플레이리스트 추가에 실패했습니다."
    );
    if (!ok) {
        if (res?.status === 409) alert(data?.message || "동일한 곡이 플레이리스트에 있습니다.");
        return;
    }

    alert("플레이리스트에 추가했습니다.");
    closePlaylistModal();
}
