import { getToken, removeToken } from "/js/auth.js";
import { resolveAudioUrl } from "/js/player-hls.js";

/**
 * ✅ 캐시 버스터
 */
let imageBust = "";

/**
 * ✅ 플레이 버튼 싱크용(이 페이지에서 마지막으로 누른 버튼)
 */
let currentPlayBtn = null;

const SONG_PLAY_API = (songId) => `/api/songs/${songId}/play`;

/* ================================
   전역 플레이어 오디오 helpers
================================ */
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
        document.querySelectorAll(".song-row").forEach((row) => {
            const btn = row.querySelector(".track-play");
            if (!btn) return;

            const url = btn.dataset.audioUrl || null;
            if (!url) return setPlayBtnState(btn, false);

            const same = isSameTrack(globalAudio, url);
            setPlayBtnState(btn, same && !globalAudio.paused);
        });

        // 내가 눌렀던 버튼이 더 이상 같은 곡이 아니면 참조 해제
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

/* ================================
   페이지 진입
================================ */
document.addEventListener("DOMContentLoaded", () => {
    const token = getToken();
    if (!token) {
        location.href = "/login";
        return;
    }

    wireGlobalAudioSync();
    loadPlaylistDetail();
});

/* ================================
   공통: JSON 안전 파서
================================ */
async function readJsonSafe(res) {
    try {
        return await res.json();
    } catch {
        return null;
    }
}

/* ================================
   공통: 401(토큰 만료) 처리
================================ */
function handleUnauthorized(message) {
    alert(message || "로그인이 필요합니다.");
    try {
        removeToken();
    } catch {}
    location.href = "/login";
}

/* ================================
   공통: API 에러 처리
================================ */
async function handleApiError(res, fallbackMessage = "요청 실패") {
    const data = await readJsonSafe(res);

    if (res.status === 401) {
        handleUnauthorized(data?.message);
        return { ok: false, data };
    }

    if (!res.ok) {
        alert(data?.message || fallbackMessage);
        return { ok: false, data };
    }

    if (data && data.success === false) {
        alert(data?.message || fallbackMessage);
        return { ok: false, data };
    }

    return { ok: true, data };
}

/* ================================
   공통: 안전한 fetch 래퍼
================================ */
async function safeFetch(url, options = {}, fallback = "요청 실패") {
    const token = getToken();
    if (!token) {
        handleUnauthorized("로그인이 필요합니다.");
        return { ok: false, data: null };
    }

    const res = await fetch(url, {
        ...options,
        headers: {
            ...(options.headers || {}),
            Authorization: token,
        },
    });

    return await handleApiError(res, fallback);
}

/* ================================
   공통: JSON 요청
================================ */
async function requestJson(url, { method = "GET", body = null } = {}, fallback = "요청 실패") {
    return await safeFetch(
        url,
        {
            method,
            headers: { "Content-Type": "application/json" },
            body: body ? JSON.stringify(body) : null,
        },
        fallback
    );
}

/* ================================
   공통: FormData 요청
================================ */
async function requestForm(url, { method = "POST", formData } = {}, fallback = "요청 실패") {
    return await safeFetch(
        url,
        {
            method,
            body: formData,
        },
        fallback
    );
}

/* ================================
   상세 조회
================================ */
async function loadPlaylistDetail() {
    const playlistId = document.getElementById("playlistId")?.value;

    if (!playlistId) {
        alert("playlistId가 없습니다. (타임리프 hidden input 확인)");
        return;
    }

    const { ok, data } = await safeFetch(`/api/playlists/${playlistId}`, {}, "플레이리스트 조회 실패");
    if (!ok) return;

    const playlist = data?.data;

    renderHeader(playlist);
    renderSongs(playlist?.songList);
}

/* ================================
   렌더: 상단(이미지/이름/버튼)
================================ */
function renderHeader(playlist) {
    const titleEl = document.getElementById("playlistName");
    const imageEl = document.getElementById("playlistImage");

    if (titleEl) titleEl.textContent = playlist?.playlistName ?? "-";

    const imageUrl = playlist?.playlistImage;
    if (imageEl) {
        if (imageUrl) {
            imageEl.style.backgroundImage = `url("${withCacheBust(imageUrl)}")`;
            imageEl.style.backgroundSize = "cover";
            imageEl.style.backgroundPosition = "center";
        } else {
            imageEl.style.backgroundImage = `url("/images/default-playlist.png")`;
            imageEl.style.backgroundSize = "cover";
            imageEl.style.backgroundPosition = "center";
        }
    }

    setupNameEdit();
    setupDeletePlaylist();
    setupImageEdit();
}

function withCacheBust(url) {
    if (!url) return url;
    if (!imageBust) return url;
    const sep = url.includes("?") ? "&" : "?";
    return `${url}${sep}v=${encodeURIComponent(imageBust)}`;
}

/* ================================
   이름 수정
================================ */
function setupNameEdit() {
    const editBtn = document.getElementById("nameEditBtn");
    const area = document.getElementById("nameEditArea");
    const input = document.getElementById("nameInput");
    if (!editBtn || !area || !input) return;

    editBtn.onclick = () => {
        area.classList.remove("hidden");
        input.value = document.getElementById("playlistName")?.textContent ?? "";
    };

    const cancelBtn = document.getElementById("nameCancelBtn");
    const saveBtn = document.getElementById("nameSaveBtn");

    cancelBtn &&
    (cancelBtn.onclick = () => {
        area.classList.add("hidden");
    });

    saveBtn &&
    (saveBtn.onclick = async () => {
        const playlistId = document.getElementById("playlistId")?.value;
        const newName = input.value.trim();

        if (!playlistId) return;
        if (!newName) {
            alert("플레이리스트 이름을 입력해주세요.");
            return;
        }

        const { ok } = await requestJson(
            `/api/playlists/${playlistId}`,
            { method: "PATCH", body: { playlistName: newName } },
            "플레이리스트 이름 수정 실패"
        );
        if (!ok) return;

        area.classList.add("hidden");
        await loadPlaylistDetail();
    });
}

/* ================================
   플레이리스트 삭제
================================ */
function setupDeletePlaylist() {
    const btn = document.getElementById("deletePlaylistBtn");
    if (!btn) return;

    btn.onclick = async () => {
        if (!confirm("플레이리스트를 삭제하시겠습니까?")) return;

        const playlistId = document.getElementById("playlistId")?.value;
        if (!playlistId) return;

        const { ok } = await requestJson(
            `/api/playlists/${playlistId}`,
            { method: "DELETE" },
            "플레이리스트 삭제 실패"
        );
        if (!ok) return;

        location.href = "/playlists";
    };
}

/* ================================
   이미지 수정
================================ */
function setupImageEdit() {
    const btn = document.getElementById("imageEditBtn");
    const input = document.getElementById("imageInput");
    if (!btn || !input) return;

    btn.onclick = () => input.click();

    input.onchange = async () => {
        const file = input.files?.[0];
        if (!file) return;

        const playlistId = document.getElementById("playlistId")?.value;
        if (!playlistId) return;

        const formData = new FormData();
        formData.append("playlistImage", file);

        const { ok } = await requestForm(
            `/api/playlists/${playlistId}/image`,
            { method: "PATCH", formData },
            "이미지 수정 실패"
        );
        if (!ok) return;

        imageBust = String(Date.now());
        input.value = "";
        await loadPlaylistDetail();
    };
}

/* =========================
   ✅ setPlayerQueue용: DOM 기준 큐 생성
   - 현재 화면의 플레이리스트 곡 순서 그대로
========================= */
function buildTracksFromDom() {
    const rows = Array.from(document.querySelectorAll(".song-row[data-id]"));
    return rows
        .map((row) => {
            const songId = Number(row.dataset.id);
            const title = row.querySelector(".song-title")?.textContent?.trim() || "Unknown";
            if (!Number.isFinite(songId)) return null;
            return { songId, title };
        })
        .filter(Boolean);
}

function registerQueue(startSongId) {
    if (typeof window.setPlayerQueue !== "function") return;
    const tracks = buildTracksFromDom();
    if (!tracks.length) return;

    window.setPlayerQueue(tracks, Number(startSongId), {
        loop: true,
        contextKey: "playlist:detail",
    });
}

/* ================================
   렌더: 수록곡 목록
================================ */
function renderSongs(songListData) {
    const container = document.getElementById("songList");
    if (!container) return;

    container.innerHTML = "";

    const actionBar = document.createElement("div");
    actionBar.className = "song-action-bar";
    actionBar.innerHTML = `
    <label>
      <input type="checkbox" id="selectAllCheckbox">
      전체 선택
    </label>
    <button id="deleteSelectedBtn" class="gray-btn">
      선택 삭제
    </button>
  `;
    container.appendChild(actionBar);

    if (!songListData || songListData.length === 0) {
        container.innerHTML += `<div style="padding:20px;color:#aaa;">곡이 없습니다.</div>`;
        attachDeleteLogic();
        return;
    }

    songListData.forEach((song) => {
        const row = document.createElement("div");
        row.className = "song-row";
        row.dataset.id = String(song.songId);

        const coverUrl = resolveImageUrl(song.albumImage);
        const title = escapeHtml(song.name ?? "-");

        row.innerHTML = `
      <div>
        <input type="checkbox" class="song-check" value="${song.songId}">
      </div>

      <div class="song-cover" style="background-image:url('${escapeHtml(coverUrl)}')"></div>

      <div class="song-info">
        <div class="song-title">${title}</div>
      </div>

      <div class="song-actions">
        <button class="track-play" type="button" aria-label="재생">▶</button>

        <span class="like-group">
          <span class="song-like-count">${song.likeCount ?? 0}</span>
          <button class="mini-heart-btn ${song.liked ? "liked" : ""}"
                  type="button"
                  aria-label="음원 좋아요">❤</button>
        </span>
      </div>
    `;

        row.addEventListener("click", (e) => {
            if (e.target.closest(".song-check")) return;
            if (e.target.closest(".track-play")) return;
            if (e.target.closest(".mini-heart-btn")) return;
            location.href = `/songs/${song.songId}/page`;
        });

        const playBtn = row.querySelector(".track-play");
        playBtn?.addEventListener("click", async (e) => {
            e.stopPropagation();
            await playViaGlobalPlayer(song.songId, song.name ?? "-", playBtn);
        });

        const heartBtn = row.querySelector(".mini-heart-btn");
        const likeCountEl = row.querySelector(".song-like-count");

        heartBtn?.addEventListener("click", async (e) => {
            e.stopPropagation();

            const { ok, data } = await safeFetch(
                `/api/songs/${song.songId}/likes`,
                { method: "POST" },
                "좋아요 처리 실패"
            );
            if (!ok) return;

            const result = data?.data;
            heartBtn.classList.toggle("liked", result?.liked === true);
            if (likeCountEl) likeCountEl.textContent = String(result?.likeCount ?? 0);
        });

        container.appendChild(row);
    });

    attachDeleteLogic();
    syncPlayButtons();
}

/* ================================
   ✅ 재생(통일 버전)
   1) /play로 url 받기
   2) setPlayerQueue로 큐 등록
   3) playSongFromPage(url,title,songId) 호출
================================ */
async function playViaGlobalPlayer(songId, title, playBtn) {
    if (typeof window.playSongFromPage !== "function") {
        alert("전역 플레이어가 아직 로드되지 않았습니다.");
        return;
    }
    if (typeof window.setPlayerQueue !== "function") {
        alert("전역 플레이어 큐 기능이 아직 로드되지 않았습니다.");
        return;
    }

    const audioUrl = await fetchStreamingUrl(songId);
    if (!audioUrl) return;

    // 싱크용 url 저장
    playBtn.dataset.audioUrl = audioUrl;

    // ✅ 큐 등록(플레이리스트 DOM 기준)
    registerQueue(songId);

    const globalAudio = getGlobalAudioEl();
    const same = isSameTrack(globalAudio, audioUrl);

    // 다른 버튼 눌렀으면 이전 버튼 원복
    if (currentPlayBtn && currentPlayBtn !== playBtn) {
        setPlayBtnState(currentPlayBtn, false);
    }
    currentPlayBtn = playBtn;

    try {
        await window.playSongFromPage(audioUrl, title, songId);

        if (!same) setPlayBtnState(playBtn, true);
        else syncPlayButtons();
    } catch (e) {
        console.error(e);
        setPlayBtnState(playBtn, false);
        currentPlayBtn = null;
        alert("재생에 실패했습니다.");
    }
}

async function fetchStreamingUrl(songId) {
    const { ok, data } = await safeFetch(
        SONG_PLAY_API(songId),
        { method: "GET" },
        "재생 정보를 불러오지 못했습니다."
    );
    if (!ok) return null;

    const raw = data?.data?.streamingUrl ?? null;
    const url = resolveAudioUrl(raw);

    if (!url) {
        alert("재생 가능한 음원 주소가 없습니다.");
        return null;
    }
    return url;
}

/* ================================
   버튼 싱크
================================ */
function syncPlayButtons() {
    const globalAudio = getGlobalAudioEl();

    document.querySelectorAll(".song-row").forEach((row) => {
        const btn = row.querySelector(".track-play");
        if (!btn) return;

        const url = btn.dataset.audioUrl || null;
        if (!url || !globalAudio) return setPlayBtnState(btn, false);

        const same = isSameTrack(globalAudio, url);
        setPlayBtnState(btn, same && !globalAudio.paused);
    });
}

/* ================================
   곡 선택 삭제
================================ */
function attachDeleteLogic() {
    const selectAll = document.getElementById("selectAllCheckbox");
    const deleteBtn = document.getElementById("deleteSelectedBtn");

    if (!selectAll || !deleteBtn) return;

    selectAll.onchange = () => {
        document.querySelectorAll(".song-check").forEach((cb) => {
            cb.checked = selectAll.checked;
        });
    };

    deleteBtn.onclick = async () => {
        const checked = [...document.querySelectorAll(".song-check:checked")];

        if (checked.length === 0) {
            alert("삭제할 곡을 선택하세요.");
            return;
        }

        if (!confirm("선택한 곡을 삭제하시겠습니까?")) return;

        const playlistId = document.getElementById("playlistId")?.value;
        if (!playlistId) return;

        const songIdSet = checked.map((cb) => Number(cb.value));

        const { ok } = await requestJson(
            `/api/playlists/${playlistId}/songs`,
            { method: "DELETE", body: { songIdSet } },
            "곡 삭제 실패"
        );
        if (!ok) return;

        await loadPlaylistDetail();
    };
}

/* ================================
   Utils
================================ */
function resolveImageUrl(path) {
    if (!path) return "/images/default.png";
    if (path.startsWith("http://") || path.startsWith("https://")) return path;
    if (path.startsWith("/")) return path;
    return `/${path}`;
}

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}