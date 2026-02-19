import { authFetch, getToken } from "./auth.js";

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

const PLAYLIST_LIST_API = "/api/playlists";
const PLAYLIST_ADD_API = (playlistId) => `/api/playlists/${playlistId}/songs`;

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

function normalizeAudioUrl(audioPath) {
    if (!audioPath) return null;
    const s = String(audioPath);
    if (s.startsWith("http://") || s.startsWith("https://")) return s;
    if (s.startsWith("/")) return s;
    return `/${s}`;
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
        if (!url) {
            setPlayBtnState(false);
            return;
        }

        const same = isSameTrack(globalAudio, url);
        const isPlaying = same && !globalAudio.paused;

        setPlayBtnState(isPlaying);
    };

    globalAudio.addEventListener("play", sync);
    globalAudio.addEventListener("pause", sync);
    globalAudio.addEventListener("ended", sync);

    // 초기 1회
    sync();
}

/* =========================
   Init
========================= */
document.addEventListener("DOMContentLoaded", async () => {
    await loadSongDetail();

    wireGlobalAudioSync();

    // ✅ 좋아요는 로그인 필요
    el.heartBtn?.addEventListener("click", async (e) => {
        e.preventDefault();
        if (!getToken()) return showLoginPopup();
        await toggleLike();
    });

    // ✅ 재생 버튼: 전역 플레이어로 재생(재생수 증가는 전역 player.js에서 songId로 처리)
    el.detailPlayBtn?.addEventListener("click", async () => {
        if (!dataCache?.audioUrl) return;

        if (typeof window.playSongFromPage !== "function") {
            alert("전역 플레이어가 아직 로드되지 않았습니다.");
            return;
        }

        try {
            await window.playSongFromPage(dataCache.audioUrl, dataCache.name, songId);
            // 버튼 상태는 전역 오디오 이벤트로 싱크됨
        } catch (e) {
            console.error(e);
            alert("재생에 실패했습니다.");
        }
    });

    // ✅ 플레이리스트 버튼(로그인 필요)
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
        : await fetch(SONG_DETAIL_API(songId));

    if (!res) return;

    const response = await res.json();
    if (!response?.success) return;

    render(response.data);
}

function render(data) {
    // ✅ audio 경로 정규화 (m3u8)
    const audioUrl = normalizeAudioUrl(data.audio);
    dataCache = { ...data, audioUrl };

    el.albumImage.src = data.albumImage || "/images/default.png";
    el.albumLink.textContent = data.albumName ?? "-";
    el.albumLink.href = data.albumId ? `/albums/${data.albumId}/page` : "#";

    el.songName.textContent = data.name ?? "-";
    el.position.textContent = data.position ?? "-";
    el.artistName.textContent = data.artistName ?? "-";

    el.genreChips.innerHTML = "";
    (data.genreList ?? []).forEach((g) => {
        const chip = document.createElement("span");
        chip.className = "chip";
        chip.textContent = g;
        el.genreChips.appendChild(chip);
    });

    el.likeCount.textContent = data.likeCount ?? 0;
    el.heartBtn.classList.toggle("liked", data.liked === true);

    el.duration.textContent = data.duration ?? "-";
    el.lang.textContent = data.lang ?? "-";
    el.speed.textContent = data.speed ?? "-";
    el.vocalInstrumental.textContent = data.vocalinstrumental ?? "-";

    el.instrumentals.textContent = data.instrumentals ?? "-";
    el.vartags.textContent = data.vartags ?? "-";

    const license = data.licenseCcurl ?? "-";
    el.licenseLink.textContent = license;
    el.licenseLink.href = license && license !== "-" ? license : "#";
}

/* =========================
   Like
========================= */
async function toggleLike() {
    const res = await authFetch(SONG_LIKE_API(songId), { method: "POST" });
    if (!res) return;

    const result = await res.json();
    if (!result?.success) return;

    const { liked, likeCount } = result.data;
    el.heartBtn.classList.toggle("liked", liked === true);
    el.likeCount.textContent = likeCount ?? 0;
}

/* =========================
   Playlist Modal
========================= */
function wirePlaylistModalClose() {
    if (!el.playlistModal) return;

    el.playlistModalClose?.addEventListener("click", closePlaylistModal);
    el.playlistModalCancel?.addEventListener("click", closePlaylistModal);

    el.playlistModal.addEventListener("click", (e) => {
        if (e.target?.dataset?.close === "true") closePlaylistModal();
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !el.playlistModal.classList.contains("hidden")) {
            closePlaylistModal();
        }
    });
}

async function openPlaylistModal() {
    el.playlistModal.classList.remove("hidden");
    el.playlistModal.setAttribute("aria-hidden", "false");

    el.playlistList.innerHTML = "불러오는 중...";

    const res = await authFetch(PLAYLIST_LIST_API, { method: "GET" });
    if (!res) return;

    const payload = await res.json();
    if (!payload?.success) {
        el.playlistList.innerHTML = "플레이리스트 조회에 실패했습니다.";
        return;
    }

    const playlists = payload.data ?? [];
    el.playlistList.innerHTML = "";

    if (playlists.length === 0) {
        el.playlistList.innerHTML = "플레이리스트가 없습니다.";
        return;
    }

    playlists.forEach((pl) => {
        const btn = document.createElement("button");
        btn.textContent = pl.playlistName;
        btn.className = "pl-item";
        btn.onclick = () => addSongToPlaylist(pl.playlistId);
        el.playlistList.appendChild(btn);
    });
}

function closePlaylistModal() {
    el.playlistModal.classList.add("hidden");
    el.playlistModal.setAttribute("aria-hidden", "true");
}

async function addSongToPlaylist(playlistId) {
    const body = { songIdSet: [Number(songId)] };

    const res = await authFetch(PLAYLIST_ADD_API(playlistId), {
        method: "POST",
        body: JSON.stringify(body),
    });

    if (!res) return;

    const payload = await res.json();

    if (!res.ok || !payload?.success) {
        alert(payload?.message || "플레이리스트 추가에 실패했습니다.");
        return;
    }

    alert("플레이리스트에 추가했습니다.");
    closePlaylistModal();
}

function showLoginPopup() {
    popup.classList.remove("hidden");
    popup.classList.add("show");

    setTimeout(() => {
        popup.classList.remove("show");
        popup.classList.add("hidden");
    }, 2000);
}