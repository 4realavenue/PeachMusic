import { authFetch } from "/js/auth.js"; // 공통 authFetch 사용 :contentReference[oaicite:2]{index=2}

const $ = (id) => document.getElementById(id);

const form = $("songCreateForm");
const audioInput = $("audio");
const pickAudioBtn = $("pickAudioBtn");
const clearAudioBtn = $("clearAudioBtn");

const previewEmpty = $("previewEmpty");
const fileInfo = $("fileInfo");
const fileName = $("fileName");
const fileMeta = $("fileMeta");

const submitBtn = $("submitBtn");

// ✅ 프론트 검증(백엔드 정책과 다르면 여기만 맞춰 바꿔)
const AUDIO_ALLOWED_EXT = ["mp3", "wav"];
const AUDIO_MAX_SIZE = 30 * 1024 * 1024;
const AUDIO_ALLOWED_MIME_PREFIX = "audio/"; // audio/*

let selectedAudio = null;

function resetAudio() {
    selectedAudio = null;
    audioInput.value = "";
    previewEmpty.classList.remove("hidden");
    fileInfo.classList.add("hidden");
    fileName.textContent = "-";
    fileMeta.textContent = "-";
}

function getExt(filename) {
    if (!filename) return "";
    const parts = filename.split(".");
    if (parts.length < 2) return "";
    return parts.pop().toLowerCase();
}

async function safeReadJson(res) {
    const ct = res.headers.get("content-type") || "";
    try {
        if (ct.includes("application/json")) return await res.json();
        const text = await res.text();
        try { return JSON.parse(text); } catch { return { message: text }; }
    } catch {
        return null;
    }
}

function showApiError(res, payload) {
    // 서버가 CommonResponse로 message 주니까 그거 우선
    const msg =
        payload?.message ||
        (res.status === 404 ? "대상을 찾을 수 없습니다." :
            res.status === 409 ? "중복 데이터가 존재합니다." :
                res.status === 400 ? "요청이 올바르지 않습니다." :
                    res.status >= 500 ? "서버 오류가 발생했습니다." :
                        "요청 실패");

    alert(msg);
}

pickAudioBtn?.addEventListener("click", () => audioInput.click());
clearAudioBtn?.addEventListener("click", resetAudio);

audioInput?.addEventListener("change", () => {
    const file = audioInput.files?.[0];
    if (!file) return;

    const ext = getExt(file.name);

    if (!AUDIO_ALLOWED_EXT.includes(ext)) {
        alert("유효하지 않은 음원 파일 형식입니다.");
        resetAudio();
        return;
    }

    if (file.size > AUDIO_MAX_SIZE) {
        alert("음원 파일 용량이 너무 큽니다.");
        resetAudio();
        return;
    }

    const ct = file.type || "";
    if (!ct.startsWith(AUDIO_ALLOWED_MIME_PREFIX)) {
        alert("유효하지 않은 음원 파일 형식입니다.");
        resetAudio();
        return;
    }

    selectedAudio = file;

    previewEmpty.classList.add("hidden");
    fileInfo.classList.remove("hidden");
    fileName.textContent = file.name;
    fileMeta.textContent = `${Math.ceil(file.size / 1024 / 1024)}MB · ${file.type || "audio/*"}`;
});

function parseGenreIdList(raw) {
    // "1,2,5" -> [1,2,5]
    if (!raw) return [];
    return raw
        .split(",")
        .map(s => s.trim())
        .filter(Boolean)
        .map(Number)
        .filter(n => Number.isFinite(n));
}

form?.addEventListener("submit", async (e) => {
    e.preventDefault();

    // ✅ 필수값
    const albumId = Number(form.albumId.value);
    const position = Number(form.position.value);
    const name = form.name.value.trim();

    if (!albumId) { alert("앨범 ID 입력은 필수입니다."); form.albumId.focus(); return; }
    if (!position && position !== 0) { alert("수록 번호 입력은 필수입니다."); form.position.focus(); return; }
    if (!name) { alert("음원 제목 입력은 필수 입니다."); form.name.focus(); return; }

    // ✅ 파일 필수 (너 캡처에도 “파일 등록은 필수입니다.” 400이 있음)
    if (!selectedAudio) {
        alert("파일 등록은 필수입니다.");
        return;
    }

    // 마지막 방어: 파일 재검증
    const ext = getExt(selectedAudio.name);
    if (!AUDIO_ALLOWED_EXT.includes(ext)) { alert("유효하지 않은 음원 파일 형식입니다."); resetAudio(); return; }
    if (selectedAudio.size > AUDIO_MAX_SIZE) { alert("음원 파일 용량이 너무 큽니다."); resetAudio(); return; }
    const ct = selectedAudio.type || "";
    if (!ct.startsWith(AUDIO_ALLOWED_MIME_PREFIX)) { alert("유효하지 않은 음원 파일 형식입니다."); resetAudio(); return; }

    const request = {
        albumId,
        position,
        name,
        duration: form.duration.value ? Number(form.duration.value) : null,
        licenseCcurl: form.licenseCcurl.value.trim() || null,
        vocalinstrumental: form.vocalinstrumental.value.trim() || null,
        lang: form.lang.value.trim() || null,
        speed: form.speed.value.trim() || null,
        genreIdList: parseGenreIdList(form.genreIdList.value),
        instruments: form.instruments.value.trim() || null,
        vartags: form.vartags.value.trim() || null
    };

    const fd = new FormData();
    // ✅ @RequestPart("request")
    fd.append("request", new Blob([JSON.stringify(request)], { type: "application/json" }));
    // ✅ @RequestPart("audio")
    fd.append("audio", selectedAudio);

    submitBtn.disabled = true;
    const prevText = submitBtn.textContent;
    submitBtn.textContent = "등록 중...";

    try {
        const res = await authFetch("/api/admin/songs", {
            method: "POST",
            body: fd
            // ❌ Content-Type 직접 설정 금지 (multipart boundary)
        });
        if (!res) return;

        const payload = await safeReadJson(res);

        if (!res.ok || payload?.success === false) {
            showApiError(res, payload);
            return;
        }

        alert(payload?.message || "음원이 생성 되었습니다.");
        location.href = "/admin/songs";

    } catch (err) {
        console.error("음원 생성 오류:", err);
        alert("네트워크/서버 오류가 발생했습니다.");
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = prevText || "등록";
    }
});
