import { authFetch, getToken } from "./auth.js";

const popup = document.getElementById("loginPopup");

let dataCache = null;

const STREAMING_BASE_URL = "https://streaming.peachmusics.com/";

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

document.addEventListener("DOMContentLoaded", async () => {

    await loadSongDetail();

    el.heartBtn?.addEventListener("click", async (e) => {
        e.preventDefault();
        if (!getToken()) return showLoginPopup();
        await toggleLike();
    });

    // 🔥 재생 버튼
    if (el.detailPlayBtn) {
        el.detailPlayBtn.addEventListener("click", () => {
            if (!dataCache?.audioUrl) return;
            window.playSongFromPage(dataCache.audioUrl, dataCache.name);
        });

        const globalAudio = document.getElementById("audioPlayer");

        globalAudio?.addEventListener("play", () => {
            if (!dataCache?.audioUrl) return;

            const isSame =
                globalAudio.src.split("/").pop() ===
                dataCache.audioUrl.split("/").pop();

            if (isSame) el.detailPlayBtn.textContent = "⏸";
        });

        globalAudio?.addEventListener("pause", () => {
            el.detailPlayBtn.textContent = "▶";
        });
    }

    // 🔥 플레이리스트 버튼
    el.addToPlaylistBtn?.addEventListener("click", async () => {
        if (!getToken()) return showLoginPopup();
        await openPlaylistModal();
    });

    wirePlaylistModalClose();
});

async function loadSongDetail() {
    const res = getToken()
        ? await authFetch(SONG_DETAIL_API(songId), { method: "GET" })
        : await fetch(SONG_DETAIL_API(songId));

    if (!res) return;

    const response = await res.json();
    if (!response.success) return;

    render(response.data);
}

function render(data) {

    let audioUrl = null;
    if (data.audio) {
        audioUrl = data.audio.startsWith("http")
            ? data.audio
            : STREAMING_BASE_URL + data.audio.replace(/^\/+/, "");
    }

    dataCache = { ...data, audioUrl };

    el.albumImage.src = data.albumImage || "/images/default.png";
    el.albumLink.textContent = data.albumName ?? "-";
    el.albumLink.href = data.albumId ? `/albums/${data.albumId}/page` : "#";

    el.songName.textContent = data.name ?? "-";
    el.position.textContent = data.position ?? "-";
    el.artistName.textContent = "-";

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

async function toggleLike() {
    const res = await authFetch(SONG_LIKE_API(songId), { method: "POST" });
    if (!res) return;

    const result = await res.json();
    if (!result.success) return;

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
        el.playlistList.innerHTML = "플레이리스트 조회 실패";
        return;
    }

    const playlists = payload.data ?? [];
    el.playlistList.innerHTML = "";

    if (playlists.length === 0) {
        el.playlistList.innerHTML = "플레이리스트가 없습니다.";
        return;
    }

    playlists.forEach(pl => {
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
        alert(payload?.message || "플레이리스트 추가 실패");
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
