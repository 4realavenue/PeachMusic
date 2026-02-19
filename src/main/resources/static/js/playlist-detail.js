import { getToken, removeToken } from "/js/auth.js";

/**
 * ✅ 캐시 버스터 상태
 * - 이미지 업로드 직후에만 bust 값을 갱신해서 "바로 반영"되게 함
 * - 페이지 새로고침할 때마다 매번 bust 하지 않음(트래픽 과다 방지)
 */
let imageBust = "";

// 페이지 진입
document.addEventListener("DOMContentLoaded", () => {
    const token = getToken();
    if (!token) {
        location.href = "/login";
        return;
    }
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
   공통: API 에러 처리
   - 백엔드 CommonResponse: { success, message, data }
   - 또는 그 외 JSON/텍스트도 최대한 처리
================================ */
async function handleApiError(res, fallbackMessage = "요청 실패") {
    const data = await readJsonSafe(res);

    // 401: 로그인 필요 (토큰 만료/없음)
    if (res.status === 401) {
        alert(data?.message || "로그인이 필요합니다.");
        try { removeToken(); } catch {}
        location.href = "/login";
        return { ok: false, data };
    }

    // 일반 실패 (HTTP 에러)
    if (!res.ok) {
        alert(data?.message || fallbackMessage);
        return { ok: false, data };
    }

    // HTTP 200이라도 success=false면 실패로 처리
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
        headers: {
            Authorization: getToken(),
        },
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

/**
 * ✅ 캐시 버스터
 * - imageBust가 비어있으면 원본 URL 사용
 * - 업로드 성공 시 imageBust 갱신 → 바로 반영
 */
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
        // 최신 데이터 다시 반영
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

        /**
         * ✅ 업로드 성공인데 이미지가 “가끔” 안 바뀌는 문제 해결:
         * - CDN/브라우저 캐시 때문에 같은 URL이면 예전 이미지가 보일 수 있음
         * - 업로드 성공 시점에만 bust 값을 갱신해서 즉시 반영
         */
        imageBust = String(Date.now());

        // input 초기화(같은 파일 다시 선택 가능)
        input.value = "";

        // 최신 데이터 다시 반영
        await loadPlaylistDetail();
    };
}

/* ================================
   렌더: 수록곡 목록
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
        row.dataset.id = song.songId;

        const coverUrl = resolveImageUrl(song.albumImage);

        row.innerHTML = `
      <div>
        <input type="checkbox" class="song-check" value="${song.songId}">
      </div>

      <div class="song-cover" style="background-image:url('${coverUrl}')"></div>

      <div class="song-info">
        <div class="song-title">${escapeHtml(song.name ?? "-")}</div>
      </div>

      <div class="song-like">❤ ${song.likeCount ?? 0}</div>
    `;

        // row 클릭 → 음원 페이지 이동 (체크박스 클릭 제외)
        row.addEventListener("click", (e) => {
            if (e.target.closest(".song-check")) return;
            location.href = `/songs/${song.songId}/page`;
        });

        container.appendChild(row);
    });

    attachDeleteLogic();
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