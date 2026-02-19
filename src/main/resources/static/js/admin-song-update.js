import { authFetch } from "/js/auth.js";

const AUDIO_ALLOWED_EXT = ["mp3", "wav"];
const AUDIO_MAX_SIZE = 30 * 1024 * 1024;

const API = {
    getOne: (songId) => `/api/songs/${songId}`,
    updateInfo: (songId) => `/api/admin/songs/${songId}`,
    updateAudio: (songId) => `/api/admin/songs/${songId}/audio`,
};

function setText(id, value) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent =
        value === null || value === undefined || value === "" ? "-" : String(value);
}

function setValue(id, value) {
    const el = document.getElementById(id);
    if (!el) return;
    el.value = value === null || value === undefined ? "" : String(value);
}

function msToSec(ms) {
    if (ms === null || ms === undefined) return "-";
    const n = Number(ms);
    if (Number.isNaN(n)) return "-";
    return String(Math.floor(n / 1000));
}

function parseCommaNumberList(text) {
    const raw = (text ?? "").trim();
    if (!raw) return null; // 비우면 수정 안함
    const list = raw
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean)
        .map((n) => Number(n))
        .filter((n) => !Number.isNaN(n));
    return list.length ? list : [];
}

function showError(targetEl, msg) {
    if (targetEl) targetEl.textContent = msg || "요청 실패";
}

async function loadSong(songId) {
    const res = await authFetch(API.getOne(songId), { method: "GET" });
    if (!res) return null;

    const body = await res.json().catch(() => null);
    if (!res.ok) return null;

    // CommonResponse면 data가 payload
    return body?.data ?? body;
}

document.addEventListener("DOMContentLoaded", async () => {
    const meta = document.getElementById("songMeta");
    const songId = meta?.dataset?.songId;

    const audioHelp = document.getElementById("audioHelp");
    const infoHelp = document.getElementById("infoHelp");

    /* =========================
       현재 값 토글
    ========================= */
    const btnToggleCurrent = document.getElementById("btnToggleCurrent");
    const currentBody = document.getElementById("currentBody");

    if (currentBody) {
        currentBody.classList.remove("hidden");
        currentBody.classList.remove("open");
    }

    btnToggleCurrent?.addEventListener("click", () => {
        if (!currentBody) return;
        const opened = currentBody.classList.toggle("open");
        btnToggleCurrent.textContent = opened ? "현재 값 숨기기" : "현재 값 보기";
    });
    /* ========================= */

    if (!songId) {
        showError(infoHelp, "songId를 찾지 못했습니다.");
        return;
    }

    /* ===== 최초 조회로 화면 채우기 (GET 실패하면 여기부터는 스킵됨) ===== */
    const dto = await loadSong(songId);

    if (!dto) {
    } else {
        // 좌측 파일 미리보기(앨범 이미지)
        const img = document.getElementById("albumImage");
        const placeholder = document.getElementById("imgPlaceholder");

        const albumImage = dto.albumImage;
        if (img) {
            // default.png 등은 숨김 처리
            if (!albumImage || String(albumImage).includes("default.png")) {
                img.style.display = "none";
                placeholder?.classList.remove("hidden");
            } else {
                img.style.display = "block";
                img.src = albumImage;
                img.onerror = () => {
                    img.style.display = "none";
                    placeholder?.classList.remove("hidden");
                };
                placeholder?.classList.add("hidden");
            }
        }

        setText("albumName", dto.albumName);
        setText("albumIdText", dto.albumId);
        setValue("currentAudio", dto.audio);

        // 현재 값 영역 (HTML에 id들이 있어야 찍힘)
        setText("curSongId", dto.songId);
        setText("curName", dto.name);
        setText("curAlbumName", dto.albumName);
        setText("curAlbumId", dto.albumId);
        setText("curPosition", dto.position);
        setText("curLikeCount", dto.likeCount);
        setText("curPlayCount", dto.playCount);
        setText("curDeleted", dto.isDeleted ? "비활성" : "활성");
        setText("curProgress", dto.progressingStatus ?? "-");
        setText("curDurationSec", msToSec(dto.duration));
        setText("curGenres", Array.isArray(dto.genreList) ? dto.genreList.join(", ") : "-");
    }

    /* ===== 음원 파일 업로드 ===== */
    const fileInput = document.getElementById("audioFile");
    const btnPick = document.getElementById("btnPickAudio");
    const btnReset = document.getElementById("btnResetAudio");
    const btnSaveAudio = document.getElementById("btnSaveAudio");
    const fileHint = document.getElementById("fileHint");

    btnPick?.addEventListener("click", () => fileInput?.click());

    btnReset?.addEventListener("click", () => {
        if (fileInput) fileInput.value = "";
        if (fileHint) fileHint.textContent = "선택된 파일 없음";
        if (audioHelp) audioHelp.textContent = "";
    });

    fileInput?.addEventListener("change", () => {
        if (audioHelp) audioHelp.textContent = "";

        const file = fileInput.files?.[0];
        if (!file) {
            if (fileHint) fileHint.textContent = "선택된 파일 없음";
            return;
        }

        const ext = (file.name.split(".").pop() || "").toLowerCase();
        if (!AUDIO_ALLOWED_EXT.includes(ext)) {
            showError(audioHelp, "유효하지 않은 음원 파일 형식입니다.");
            fileInput.value = "";
            if (fileHint) fileHint.textContent = "선택된 파일 없음";
            return;
        }

        if (file.size > AUDIO_MAX_SIZE) {
            showError(audioHelp, "음원 파일 용량이 너무 큽니다.");
            fileInput.value = "";
            if (fileHint) fileHint.textContent = "선택된 파일 없음";
            return;
        }

        if (fileHint) fileHint.textContent = file.name;
    });

    btnSaveAudio?.addEventListener("click", async () => {
        if (audioHelp) audioHelp.textContent = "";

        const file = fileInput?.files?.[0];
        if (!file) {
            showError(audioHelp, "파일 등록은 필수입니다.");
            return;
        }

        const formData = new FormData();
        formData.append("audio", file);

        const res = await authFetch(API.updateAudio(songId), {
            method: "PATCH",
            body: formData,
        });
        if (!res) return;

        const body = await res.json().catch(() => null);
        if (!res.ok) {
            showError(audioHelp, body?.message ?? "파일 업로드에 실패했습니다.");
            return;
        }

        showError(audioHelp, "음원 파일이 저장되었습니다.");

        const data = body?.data ?? body;
        if (data?.audio) setValue("currentAudio", data.audio);
    });

    /* ===== 기본 정보 수정 ===== */
    const el = {
        songName: document.getElementById("songName"),
        albumId: document.getElementById("albumId"),
        position: document.getElementById("position"),
        duration: document.getElementById("duration"), // 초 입력
        licenseCcurl: document.getElementById("licenseCcurl"),
        vocalinstrumental: document.getElementById("vocalinstrumental"),
        lang: document.getElementById("lang"),
        speed: document.getElementById("speed"),
        genreIdList: document.getElementById("genreIdList"),
        instruments: document.getElementById("instruments"),
        vartags: document.getElementById("vartags"),
        btnSaveInfo: document.getElementById("btnSaveInfo"),
        btnCancelInfo: document.getElementById("btnCancelInfo"),
    };

    el.btnCancelInfo?.addEventListener("click", () => {
        Object.values(el).forEach((node) => {
            if (!node) return;
            if (node.tagName === "INPUT" || node.tagName === "TEXTAREA") node.value = "";
        });
        if (infoHelp) infoHelp.textContent = "";
    });

    el.btnSaveInfo?.addEventListener("click", async () => {
        if (infoHelp) infoHelp.textContent = "";

        const payload = {};

        const name = (el.songName?.value ?? "").trim();
        const albumId = (el.albumId?.value ?? "").trim();
        const position = (el.position?.value ?? "").trim();
        const durationSec = (el.duration?.value ?? "").trim();
        const licenseCcurl = (el.licenseCcurl?.value ?? "").trim();
        const vocalinstrumental = (el.vocalinstrumental?.value ?? "").trim();
        const lang = (el.lang?.value ?? "").trim();
        const speed = (el.speed?.value ?? "").trim();
        const instruments = (el.instruments?.value ?? "").trim();
        const vartags = (el.vartags?.value ?? "").trim();

        const genreIdList = parseCommaNumberList(el.genreIdList?.value);

        if (name) payload.name = name;
        if (albumId) payload.albumId = Number(albumId);
        if (position) payload.position = Number(position);

        if (durationSec) {
            const sec = Number(durationSec);
            if (!Number.isNaN(sec)) payload.duration = Math.floor(sec * 1000);
        }

        if (licenseCcurl) payload.licenseCcurl = licenseCcurl;
        if (vocalinstrumental) payload.vocalinstrumental = vocalinstrumental;
        if (lang) payload.lang = lang;
        if (speed) payload.speed = speed;

        if (genreIdList !== null) payload.genreIdList = genreIdList;

        if (instruments) payload.instruments = instruments;
        if (vartags) payload.vartags = vartags;

        ["albumId", "position", "duration"].forEach((k) => {
            if (k in payload && Number.isNaN(payload[k])) delete payload[k];
        });

        if (Object.keys(payload).length === 0) {
            showError(infoHelp, "수정할 값이 없습니다.");
            return;
        }

        const res = await authFetch(API.updateInfo(songId), {
            method: "PATCH",
            body: JSON.stringify(payload),
        });
        if (!res) return;

        const body = await res.json().catch(() => null);
        if (!res.ok) {
            showError(infoHelp, body?.message ?? "기본 정보 수정 실패");
            return;
        }

        showError(infoHelp, "기본 정보가 저장되었습니다.");
    });

    /* ===== 현재 음원 키/URL 유틸 ===== */
    const currentAudio = document.getElementById("currentAudio");
    const btnCopyAudio = document.getElementById("btnCopyAudio");
    const btnOpenAudio = document.getElementById("btnOpenAudio");

    btnCopyAudio?.addEventListener("click", async () => {
        const v = currentAudio?.value ?? "";
        if (!v) return;
        try {
            await navigator.clipboard.writeText(v);
        } catch {
            currentAudio.select();
            document.execCommand("copy");
        }
    });

    btnOpenAudio?.addEventListener("click", (e) => {
        const v = currentAudio?.value ?? "";
        if (!v) {
            e.preventDefault();
            return;
        }

        if (v.startsWith("http://") || v.startsWith("https://")) {
            btnOpenAudio.href = v;
        } else {
            btnOpenAudio.href = `https://streaming.peachmusics.com/${v.replace(/^\/+/, "")}`;
        }
    });
});
