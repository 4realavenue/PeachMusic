import { authFetch, getToken, removeToken } from "./auth.js";
import { resolveAudioUrl } from "/js/player-hls.js";

const popup = document.getElementById("loginPopup");

let dataCache = null;

const el = {
    albumImage: document.getElementById("albumImage"),
    songName: document.getElementById("songName"),
    artistName: document.getElementById("artistName"), // ✅ 컨테이너(span/div)
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
    if (!res) return { ok: false, data: null, res: null };

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

/**
 * ✅ 핵심: 서버에서 &quot; 같은 엔티티로 내려오는 문자열을 " 로 복원
 */
function decodeHtmlEntities(str) {
    if (str == null) return "";
    const txt = document.createElement("textarea");
    txt.innerHTML = String(str);
    return txt.value;
}

/* =========================
   ✅ Artist links render
   - 백엔드: artistList: [{artistId, artistName}, ...]
   - 링크: /artists/{artistId}
========================= */
function renderArtistLinks(data) {
    if (!el.artistName) return;

    const list = Array.isArray(data?.artistList) ? data.artistList : [];

    el.artistName.innerHTML = "";

    // 하위호환: artistList 없으면 기존 문자열
    if (!list.length) {
        el.artistName.textContent = decodeHtmlEntities(data?.artistName ?? "-");
        return;
    }

    list.forEach((a, idx) => {
        const id = a?.artistId ?? a?.id ?? a?.artist_id ?? null;
        const name = decodeHtmlEntities(a?.artistName ?? a?.name ?? "-");

        if (idx > 0) {
            el.artistName.appendChild(document.createTextNode(", "));
        }

        if (id != null && String(id).length > 0) {
            const link = document.createElement("a");
            link.className = "link";
            link.href = `/artists/${encodeURIComponent(String(id))}`;
            link.textContent = name;
            link.addEventListener("click", (e) => e.stopPropagation());
            el.artistName.appendChild(link);
        } else {
            const span = document.createElement("span");
            span.textContent = name;
            el.artistName.appendChild(span);
        }
    });
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

        const title = decodeHtmlEntities(dataCache?.name ?? "Unknown");
        setQueueForThisSong(songId, title);

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

    // ✅ 앨범명 디코딩해서 출력
    if (el.albumLink) {
        el.albumLink.textContent = decodeHtmlEntities(data.albumName ?? "-");
        el.albumLink.href = data.albumId ? `/albums/${data.albumId}/page` : "#";
    }

    // ✅ 곡명 디코딩해서 출력 (여기가 스샷에서 깨지는 핵심)
    if (el.songName) el.songName.textContent = decodeHtmlEntities(data.name ?? "-");

    if (el.position) el.position.textContent = data.position ?? "-";

    // ✅ 아티스트 링크도 디코딩해서 출력
    renderArtistLinks(data);

    if (el.genreChips) {
        el.genreChips.innerHTML = "";
        (data.genreList ?? []).forEach((g) => {
            const chip = document.createElement("span");
            chip.className = "chip";
            chip.textContent = decodeHtmlEntities(g);
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

    if (el.lang) el.lang.textContent = decodeHtmlEntities(data.lang ?? "-");
    if (el.speed) el.speed.textContent = decodeHtmlEntities(data.speed ?? "-");
    if (el.vocalInstrumental) el.vocalInstrumental.textContent = decodeHtmlEntities(getVocalInstrumentalValue(data) ?? "-");

    // 태그 문자열도 엔티티가 섞여있을 수 있어서 디코딩
    if (el.instrumentals) el.instrumentals.textContent = decodeHtmlEntities(data.instrumentals ?? "-");
    if (el.vartags) el.vartags.textContent = decodeHtmlEntities(data.vartags ?? "-");

    if (el.licenseLink) {
        const license = data.licenseCcurl ?? "-";
        el.licenseLink.textContent = decodeHtmlEntities(license);
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
        btn.textContent = decodeHtmlEntities(pl.playlistName ?? "-");
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