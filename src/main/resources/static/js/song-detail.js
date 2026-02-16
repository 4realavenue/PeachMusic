import { authFetch, getToken } from "./auth.js";

const popup = document.getElementById("loginPopup");

const el = {
    albumImage: document.getElementById("albumImage"),
    songName: document.getElementById("songName"),
    artistName: document.getElementById("artistName"),
    albumLink: document.getElementById("albumLink"),
    position: document.getElementById("position"),
    genreChips: document.getElementById("genreChips"),
    audioPlayer: document.getElementById("audioPlayer"),

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

    // playlist modal
    playlistModal: document.getElementById("playlistModal"),
    playlistList: document.getElementById("playlistList"),
    playlistModalClose: document.getElementById("playlistModalClose"),
    playlistModalCancel: document.getElementById("playlistModalCancel"),
};

const SONG_DETAIL_API = (id) => `/api/songs/${id}`;
const SONG_LIKE_API = (id) => `/api/songs/${id}/likes`;

const PLAYLIST_LIST_API = "/api/playlists";
const PLAYLIST_ADD_SONG_API = (playlistId) => `/api/playlists/${playlistId}/songs`;

document.addEventListener("DOMContentLoaded", async () => {

    if (!el.heartBtn) {
        console.error("❌ heartBtn을 찾지 못함: HTML에 id='heartBtn' 있는지 확인");
        return;
    }

    // ✅ 상세 조회
    await loadSongDetail();

    // ✅ 좋아요 클릭 (비로그인이면 팝업)
    el.heartBtn.addEventListener("click", async (e) => {
        e.preventDefault();

        const hasToken = !!getToken();
        if (!hasToken) {
            showLoginPopup();
            return;
        }

        await toggleLike();
    });

    // ✅ 플레이리스트 추가 (+) 버튼
    if (el.addToPlaylistBtn) {
        el.addToPlaylistBtn.addEventListener("click", async () => {
            const hasToken = !!getToken();
            if (!hasToken) {
                showLoginPopup();
                return;
            }

            await openPlaylistModal();
        });
    }

    // ✅ 모달 닫기 wiring
    wirePlaylistModalClose();

    // ✅ 플레이어 바인딩
    bindPlayer();
});

async function loadSongDetail() {
    try {
        const hasToken = !!getToken();

        const res = hasToken
            ? await authFetch(SONG_DETAIL_API(songId), { method: "GET" })
            : await fetch(SONG_DETAIL_API(songId));

        if (!res) return;

        const response = await res.json();

        if (!response.success) {
            alert(response.message || "음원 조회 실패");
            return;
        }

        render(response.data);
    } catch (e) {
        console.error(e);
        alert("서버 오류");
    }
}

function render(data) {
    el.albumImage.src = data.albumImage || "/images/default.png";
    el.albumLink.textContent = data.albumName ?? "-";
    el.albumLink.href = data.albumId ? `/albums/${data.albumId}/page` : "#";

    el.songName.textContent = data.name ?? "-";
    el.position.textContent = data.position ?? "-";

    // DTO에 없으면 "-"
    el.artistName.textContent = "-";

    // ✅ 발매일: releaseDate 기준 (구버전/중첩 응답 fallback)
    const releaseDate =
        data.releaseDate ??
        data.albumReleaseDate ??
        data.album?.albumReleaseDate ??
        null;

    // ⚠️ 현재 이 파일에는 발매일을 찍는 DOM(el.releaseDate 같은)이 없어서
    // 실제로 화면에 날짜를 보여주는 요소가 있다면, 그 요소에 releaseDate를 넣어주면 됨.
    // 예: el.releaseDate.textContent = releaseDate ?? "-";

    // 오디오
    el.audioPlayer.src = data.audio ?? "";

    // 장르
    el.genreChips.innerHTML = "";
    (data.genreList ?? []).forEach((g) => {
        const chip = document.createElement("span");
        chip.className = "chip";
        chip.textContent = g;
        el.genreChips.appendChild(chip);
    });

    // 좋아요 상태
    el.likeCount.textContent = data.likeCount ?? 0;
    el.heartBtn.classList.toggle("liked", data.liked === true);

    // 상세 필드
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
    try {
        const res = await authFetch(SONG_LIKE_API(songId), { method: "POST" });
        if (!res) return;

        const result = await res.json();
        if (!result.success) {
            alert(result.message || "좋아요 요청 실패");
            return;
        }

        const { liked, likeCount } = result.data;

        el.heartBtn.classList.toggle("liked", liked === true);
        el.likeCount.textContent = likeCount ?? 0;
    } catch (e) {
        console.error(e);
        alert("서버 오류");
    }
}

/* =========================
   Playlist Modal
========================= */
function wirePlaylistModalClose() {
    if (!el.playlistModal) return;

    // backdrop 클릭 닫기
    el.playlistModal.addEventListener("click", (e) => {
        if (e.target?.dataset?.close === "true") closePlaylistModal();
    });

    el.playlistModalClose?.addEventListener("click", closePlaylistModal);
    el.playlistModalCancel?.addEventListener("click", closePlaylistModal);

    // ESC 닫기
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !el.playlistModal.classList.contains("hidden")) {
            closePlaylistModal();
        }
    });
}

async function openPlaylistModal() {
    if (!el.playlistModal || !el.playlistList) return;

    el.playlistModal.classList.remove("hidden");
    el.playlistModal.setAttribute("aria-hidden", "false");

    el.playlistList.innerHTML = `<div class="pl-empty">불러오는 중...</div>`;

    const res = await authFetch(PLAYLIST_LIST_API, { method: "GET" });
    if (!res) return;

    let payload = null;
    try {
        payload = await res.json();
    } catch {
        payload = null;
    }

    if (!res.ok || !payload?.success) {
        el.playlistList.innerHTML = `<div class="pl-empty">플레이리스트 목록 조회 실패</div>`;
        return;
    }

    const playlists = payload.data ?? [];

    if (playlists.length === 0) {
        el.playlistList.innerHTML = `<div class="pl-empty">플레이리스트가 없습니다. 먼저 생성해주세요.</div>`;
        return;
    }

    el.playlistList.innerHTML = "";

    playlists.forEach((pl) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "pl-item";
        btn.dataset.id = pl.playlistId;

        const imgHtml = pl.playlistImage
            ? `<img src="${pl.playlistImage}" alt="">`
            : `<div class="pl-thumb-ph">♪</div>`;

        btn.innerHTML = `
            <div class="pl-thumb">${imgHtml}</div>
            <div class="pl-meta">
                <div class="pl-name">${pl.playlistName ?? "-"}</div>
                <div class="pl-sub">클릭하면 이 플레이리스트에 추가</div>
            </div>
        `;

        btn.addEventListener("click", async () => {
            await addSongToPlaylist(pl.playlistId);
        });

        el.playlistList.appendChild(btn);
    });
}

function closePlaylistModal() {
    if (!el.playlistModal) return;
    el.playlistModal.classList.add("hidden");
    el.playlistModal.setAttribute("aria-hidden", "true");
}

async function addSongToPlaylist(playlistId) {
    try {
        // ✅ Set<Long> → 배열로 보내면 스프링이 Set으로 바인딩하는 게 일반적
        const body = { songIdSet: [Number(songId)] };

        const res = await authFetch(PLAYLIST_ADD_SONG_API(playlistId), {
            method: "POST",
            body: JSON.stringify(body),
        });

        // 401이면 authFetch가 리다이렉트 + undefined 반환
        if (!res) return;

        let payload = null;
        try {
            payload = await res.json();
        } catch {
            payload = null;
        }

        // ✅ 에러 코드별 UX 처리 (네가 준 명세 그대로)
        if (!res.ok) {
            const msg = payload?.message;

            if (res.status === 400) {
                alert(msg || "플레이리스트에 담을 음원을 입력해 주세요.");
                return;
            }
            if (res.status === 403) {
                alert(msg || "접근 권한이 없습니다.");
                return;
            }
            if (res.status === 404) {
                alert(msg || "플레이리스트가 존재하지 않습니다.");
                return;
            }
            if (res.status === 409) {
                alert(msg || "동일한 곡이 플레이리스트에 있습니다.");
                return;
            }

            alert(msg || "플레이리스트 추가 실패");
            return;
        }

        if (!payload?.success) {
            alert(payload?.message || "플레이리스트 추가 실패");
            return;
        }

        const addedCount = payload?.data?.addedCount ?? 0;

        if (addedCount > 0) {
            alert("플레이리스트에 추가했습니다.");
        } else {
            // addedCount가 0이면 보통 이미 있거나 추가된 게 없는 케이스
            alert("이미 플레이리스트에 있는 곡일 수 있어요.");
        }

        closePlaylistModal();

    } catch (e) {
        console.error(e);
        alert("서버 오류");
    }
}

/* =========================
   Apple Music 스타일 플레이어
========================= */
function bindPlayer() {
    const audio = document.getElementById("audioPlayer");
    const playBtn = document.getElementById("playBtn");
    const progressBar = document.getElementById("progressBar");
    const currentTimeEl = document.getElementById("currentTime");
    const durationTimeEl = document.getElementById("durationTime");

    if (!audio || !playBtn || !progressBar || !currentTimeEl || !durationTimeEl) return;

    playBtn.addEventListener("click", () => {
        if (audio.paused) {
            audio.play();
            playBtn.textContent = "❚❚";
        } else {
            audio.pause();
            playBtn.textContent = "▶";
        }
    });

    audio.addEventListener("loadedmetadata", () => {
        progressBar.max = Math.floor(audio.duration || 0);
        durationTimeEl.textContent = formatTime(audio.duration || 0);
    });

    audio.addEventListener("timeupdate", () => {
        progressBar.value = Math.floor(audio.currentTime || 0);
        currentTimeEl.textContent = formatTime(audio.currentTime || 0);
    });

    progressBar.addEventListener("input", () => {
        audio.currentTime = Number(progressBar.value || 0);
    });

    audio.addEventListener("pause", () => (playBtn.textContent = "▶"));
    audio.addEventListener("play", () => (playBtn.textContent = "❚❚"));
}

function formatTime(sec) {
    const s = Math.floor(Number(sec) || 0);
    const m = Math.floor(s / 60);
    const r = String(s % 60).padStart(2, "0");
    return `${m}:${r}`;
}

function showLoginPopup() {
    popup.classList.remove("hidden");
    popup.classList.add("show");

    setTimeout(() => {
        popup.classList.remove("show");
        popup.classList.add("hidden");
    }, 2000);
}
