import { getToken, removeToken } from "/js/auth.js";

/**
 * ✅ 캐시 버스터 상태
 * - 이미지 업로드 직후에만 bust 값을 갱신해서 "바로 반영"되게 함
 * - 페이지 새로고침할 때마다 매번 bust 하지 않음(트래픽 과다 방지)
 */
let imageBust = "";

/* =========================
   ✅ 전역 플레이어(공용)
========================= */
let currentSongId = null;
let currentPlayBtn = null;

const SONG_DETAIL_API = (songId) => `/api/songs/${songId}`;

/* 전역 플레이어 오디오 찾기 */
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

            if (!url) {
                setPlayBtnState(btn, false);
                return;
            }

            const same = isSameTrack(globalAudio, url);
            const isPlaying = same && !globalAudio.paused;
            setPlayBtnState(btn, isPlaying);
        });

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
    try { return await res.json(); } catch { return null; }
}

/* ================================
   공통: API 에러 처리
================================ */
async function handleApiError(res, fallbackMessage = "요청 실패") {
    const data = await readJsonSafe(res);

    if (res.status === 401) {
        alert(data?.message || "로그인이 필요합니다.");
        try { removeToken(); } catch {}
        location.href = "/login";
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
   공통: JSON 요청
================================ */
async function requestJson(url, { method = "GET", body = null } = {}, fallback = "요청 실패") {
    const res = await fetch(url, {
        method,
        headers: {
            Authorization: getToken(),
            "Content-Type": "application/json",
        },
        body: body ? JSON.stringify(body) : null,
    });

    return await handleApiError(res, fallback);
}

/* ================================
   공통: FormData 요청 (파일 업로드)
================================ */
async function requestForm(url, { method = "POST", formData } = {}, fallback = "요청 실패") {
    const res = await fetch(url, {
        method,
        headers: { Authorization: getToken() },
        body: formData,
    });

    return await handleApiError(res, fallback);
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

    const res = await fetch(`/api/playlists/${playlistId}`, {
        headers: { Authorization: getToken() },
    });

    const { ok, data } = await handleApiError(res, "플레이리스트 조회 실패");
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

    titleEl.textContent = playlist?.playlistName ?? "-";

    const imageUrl = playlist?.playlistImage;

    if (imageUrl) {
        imageEl.style.backgroundImage = `url("${withCacheBust(imageUrl)}")`;
        imageEl.style.backgroundSize = "cover";
        imageEl.style.backgroundPosition = "center";
    } else {
        imageEl.style.backgroundImage = `url("/images/default-playlist.png")`;
        imageEl.style.backgroundSize = "cover";
        imageEl.style.backgroundPosition = "center";
    }

    setupNameEdit();
    setupDeletePlaylist();
    setupImageEdit();
}

/* ✅ 캐시 버스터 */
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

    editBtn.onclick = () => {
        area.classList.remove("hidden");
        input.value = document.getElementById("playlistName").textContent;
    };

    document.getElementById("nameCancelBtn").onclick = () => {
        area.classList.add("hidden");
    };

    document.getElementById("nameSaveBtn").onclick = async () => {
        const playlistId = document.getElementById("playlistId").value;
        const newName = input.value.trim();

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
    };
}

/* ================================
   플레이리스트 삭제
================================ */
function setupDeletePlaylist() {
    document.getElementById("deletePlaylistBtn").onclick = async () => {
        if (!confirm("플레이리스트를 삭제하시겠습니까?")) return;

        const playlistId = document.getElementById("playlistId").value;

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

    btn.onclick = () => input.click();

    input.onchange = async () => {
        const file = input.files?.[0];
        if (!file) return;

        const playlistId = document.getElementById("playlistId").value;

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

/* ================================
   ✅ 렌더: 수록곡 목록 (재생 - [숫자+하트])
   ✅ 숫자/하트 간격은 HTML에서 like-group으로 묶어서 CSS gap=4로 제어
================================ */
function renderSongs(songList) {
    const container = document.getElementById("songList");
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

    if (!songList || songList.length === 0) {
        container.innerHTML += `<div style="padding:20px;color:#aaa;">곡이 없습니다.</div>`;
        attachDeleteLogic();
        return;
    }

    songList.forEach((song) => {
        const row = document.createElement("div");
        row.className = "song-row";
        row.dataset.id = String(song.songId);

        const coverUrl = resolveImageUrl(song.albumImage);
        const title = escapeHtml(song.name ?? "-");

        row.innerHTML = `
            <div>
                <input type="checkbox" class="song-check" value="${song.songId}">
            </div>

            <div class="song-cover" style="background-image:url('${coverUrl}')"></div>

            <div class="song-info">
                <div class="song-title">${title}</div>
            </div>

            <div class="song-actions">
                <button class="track-play" type="button" aria-label="재생">▶</button>

                <!-- ✅ 숫자+하트 묶음 -->
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
        playBtn.addEventListener("click", async (e) => {
            e.stopPropagation();
            await playViaGlobalPlayer(song.songId, song.name ?? "-", playBtn);
        });

        const heartBtn = row.querySelector(".mini-heart-btn");
        const likeCountEl = row.querySelector(".song-like-count");

        heartBtn.addEventListener("click", async (e) => {
            e.stopPropagation();

            try {
                const res = await fetch(`/api/songs/${song.songId}/likes`, {
                    method: "POST",
                    headers: { Authorization: getToken() },
                });

                const { ok, data } = await handleApiError(res, "좋아요 처리 실패");
                if (!ok) return;

                const result = data?.data;
                heartBtn.classList.toggle("liked", result?.liked === true);
                likeCountEl.textContent = String(result?.likeCount ?? 0);
            } catch (err) {
                console.error(err);
            }
        });

        container.appendChild(row);
    });

    attachDeleteLogic();
    syncPlayButtons();
}

/* ================================
   ✅ 전역 플레이어로 재생
================================ */
async function playViaGlobalPlayer(songId, title, playBtn) {
    if (typeof window.playSongFromPage !== "function") {
        alert("전역 플레이어가 아직 로드되지 않았습니다.");
        return;
    }

    const audioUrl = await fetchAudioUrl(songId);
    if (!audioUrl) return;

    playBtn.dataset.audioUrl = audioUrl;

    const globalAudio = getGlobalAudioEl();
    const same = isSameTrack(globalAudio, audioUrl);

    if (currentPlayBtn && currentPlayBtn !== playBtn) {
        setPlayBtnState(currentPlayBtn, false);
    }
    currentSongId = songId;
    currentPlayBtn = playBtn;

    try {
        await window.playSongFromPage(audioUrl, title, songId);

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
        const res = await fetch(SONG_DETAIL_API(songId), {
            headers: { Authorization: getToken() },
        });

        const { ok, data } = await handleApiError(res, "음원 정보를 불러오지 못했습니다.");
        if (!ok) return null;

        const raw = data?.data?.audio;
        const url = resolveAudioUrlSimple(raw);

        if (!url) {
            alert("재생 가능한 음원 주소가 없습니다.");
            return null;
        }

        return url;
    } catch (e) {
        console.error(e);
        alert("음원 정보를 불러오지 못했습니다.");
        return null;
    }
}

/* ✅ 최소 정규화 */
function resolveAudioUrlSimple(audioPath) {
    if (!audioPath) return null;
    const s = String(audioPath);
    if (s.startsWith("http://") || s.startsWith("https://")) return s;
    if (s.startsWith("/")) return s;
    return `/${s}`;
}

/* ================================
   ✅ 버튼 싱크 (전역 오디오 기준)
================================ */
function syncPlayButtons() {
    const globalAudio = getGlobalAudioEl();

    document.querySelectorAll(".song-row").forEach((row) => {
        const btn = row.querySelector(".track-play");
        if (!btn) return;

        const url = btn.dataset.audioUrl || null;

        if (!url || !globalAudio) {
            setPlayBtnState(btn, false);
            return;
        }

        const same = isSameTrack(globalAudio, url);
        const isPlaying = same && !globalAudio.paused;

        setPlayBtnState(btn, isPlaying);
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

        const playlistId = document.getElementById("playlistId").value;
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