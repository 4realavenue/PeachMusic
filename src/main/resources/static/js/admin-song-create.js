import { authFetch } from "/js/auth.js";

const $ = (id) => document.getElementById(id);

const form = $("songCreateForm");
const audioInput = $("audio");
const pickAudioBtn = $("pickAudioBtn");
const clearAudioBtn = $("clearAudioBtn");
const fileHint = $("fileHint");
const audioHelp = $("audioHelp");
const submitBtn = $("submitBtn");

const AUDIO_ALLOWED_EXT = ["mp3", "wav"];
const AUDIO_MAX_SIZE = 30 * 1024 * 1024;

let selectedAudio = null;

function getExt(filename) {
    if (!filename) return "";
    const parts = filename.split(".");
    if (parts.length < 2) return "";
    return parts.pop().toLowerCase();
}

function setHelp(msg) {
    if (audioHelp) audioHelp.textContent = msg || "";
}

function resetAudio() {
    selectedAudio = null;
    if (audioInput) audioInput.value = "";
    if (fileHint) fileHint.textContent = "선택된 파일 없음";
    setHelp("");
}

pickAudioBtn?.addEventListener("click", () => audioInput?.click());
clearAudioBtn?.addEventListener("click", resetAudio);

audioInput?.addEventListener("change", () => {
    setHelp("");

    const file = audioInput.files?.[0];
    if (!file) {
        resetAudio();
        return;
    }

    const ext = getExt(file.name);
    if (!AUDIO_ALLOWED_EXT.includes(ext)) {
        setHelp("유효하지 않은 음원 파일 형식입니다. (mp3/wav)");
        resetAudio();
        return;
    }

    if (file.size > AUDIO_MAX_SIZE) {
        setHelp("음원 파일 용량이 너무 큽니다. (최대 30MB)");
        resetAudio();
        return;
    }

    selectedAudio = file;
    if (fileHint) fileHint.textContent = file.name;
});

function parseGenreIdList(raw) {
    const v = (raw ?? "").trim();
    if (!v) return [];
    return v
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean)
        .map(Number)
        .filter((n) => Number.isFinite(n));
}

async function safeReadJson(res) {
    try {
        return await res.json();
    } catch {
        return null;
    }
}

function showApiError(res, payload) {
    const msg =
        payload?.message ||
        (res.status === 404 ? "대상을 찾을 수 없습니다." :
            res.status === 409 ? "중복 데이터가 존재합니다." :
                res.status === 400 ? "요청이 올바르지 않습니다." :
                    res.status >= 500 ? "서버 오류가 발생했습니다." :
                        "요청 실패");
    setHelp(msg);
}

form?.addEventListener("submit", async (e) => {
    e.preventDefault();
    setHelp("");

    const albumId = Number(form.albumId.value);
    const position = Number(form.position.value);
    const name = form.name.value.trim();

    if (!albumId) { setHelp("앨범 ID 입력은 필수입니다."); form.albumId.focus(); return; }
    if (!position && position !== 0) { setHelp("수록 번호 입력은 필수입니다."); form.position.focus(); return; }
    if (!name) { setHelp("음원 제목 입력은 필수입니다."); form.name.focus(); return; }

    if (!selectedAudio) {
        setHelp("파일 등록은 필수입니다.");
        return;
    }

    // 최종 안전검증
    const ext = getExt(selectedAudio.name);
    if (!AUDIO_ALLOWED_EXT.includes(ext)) { setHelp("유효하지 않은 음원 파일 형식입니다. (mp3/wav)"); resetAudio(); return; }
    if (selectedAudio.size > AUDIO_MAX_SIZE) { setHelp("음원 파일 용량이 너무 큽니다. (최대 30MB)"); resetAudio(); return; }

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
    fd.append("request", new Blob([JSON.stringify(request)], { type: "application/json" }));
    fd.append("audio", selectedAudio);

    submitBtn.disabled = true;
    const prevText = submitBtn.textContent;
    submitBtn.textContent = "등록 중...";

    try {
        const res = await authFetch("/api/admin/songs", {
            method: "POST",
            body: fd
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
        console.error(err);
        setHelp("네트워크/서버 오류가 발생했습니다.");
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = prevText || "등록";
    }
});
