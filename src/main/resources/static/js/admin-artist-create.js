import { authFetch } from "/js/auth.js";

const $ = (id) => document.getElementById(id);

const form = $("artistCreateForm");
const profileInput = $("profileImage");
const pickBtn = $("pickImageBtn");
const clearBtn = $("clearImageBtn");
const preview = $("imagePreview");
const previewEmpty = $("previewEmpty");
const previewImg = $("previewImg");
const bioCount = $("bioCount");
const submitBtn = $("submitBtn");

// 정책(백엔드 FileStorageService와 동일하게)
const IMAGE_ALLOWED_EXT = ["jpg", "jpeg", "png"];
const IMAGE_MAX_SIZE = 5 * 1024 * 1024; // 5MB
const IMAGE_ALLOWED_MIME_PREFIX = "image/"; // image/* 만 허용

let selectedFile = null;
let previewObjectUrl = null;

/* ============================
   Helpers
============================ */

function hideEmptyText() {
    if (!previewEmpty) return;
    previewEmpty.classList.add("hidden");
}

function showEmptyText() {
    if (!previewEmpty) return;
    previewEmpty.classList.remove("hidden");
}

function revokePreviewUrl() {
    if (previewObjectUrl) {
        URL.revokeObjectURL(previewObjectUrl);
        previewObjectUrl = null;
    }
}

function resetImage() {
    selectedFile = null;

    // file input 리셋
    if (profileInput) profileInput.value = "";

    // 기존 objectURL 정리
    revokePreviewUrl();

    // ✅ 미리보기 초기화 (배경/이미지 둘 다 비우기)
    if (preview) preview.style.backgroundImage = "";
    if (previewImg) {
        previewImg.src = "";
        previewImg.classList.add("hidden");
    }

    // ✅ "이미지 미리보기" 다시 보이게
    showEmptyText();
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
        try {
            return JSON.parse(text);
        } catch {
            return { message: text };
        }
    } catch {
        return null;
    }
}

function showApiError(res, payload) {
    const msg =
        payload?.message ||
        (res.status === 401 ? "로그인이 필요합니다." :
            res.status === 403 ? "권한이 없습니다." :
                res.status === 400 ? "요청이 올바르지 않습니다." :
                    res.status >= 500 ? "서버 오류가 발생했습니다." :
                        "요청 실패");

    alert(msg);
}

/* ============================
   Image UI
============================ */

pickBtn?.addEventListener("click", () => profileInput?.click());

clearBtn?.addEventListener("click", () => resetImage());

profileInput?.addEventListener("change", () => {
    const file = profileInput.files?.[0];
    if (!file) return;

    const ext = getExt(file.name);

    // 1) 확장자 체크 (webp 차단)
    if (!IMAGE_ALLOWED_EXT.includes(ext)) {
        alert("유효하지 않은 이미지 파일 형식입니다. (jpg / jpeg / png만 가능)");
        resetImage();
        return;
    }

    // 2) 용량 체크
    if (file.size > IMAGE_MAX_SIZE) {
        alert("이미지 파일 용량이 너무 큽니다. (최대 5MB)");
        resetImage();
        return;
    }

    // 3) mime 체크(브라우저가 알려주는 content-type)
    const ct = file.type || "";
    if (!ct.startsWith(IMAGE_ALLOWED_MIME_PREFIX)) {
        alert("유효하지 않은 이미지 파일입니다.");
        resetImage();
        return;
    }

    selectedFile = file;

    // ✅ 기존 objectURL 정리 후 새로 생성
    revokePreviewUrl();
    previewObjectUrl = URL.createObjectURL(file);

    // ✅ "이미지 미리보기" 글씨 숨김
    hideEmptyText();

    // ✅ 미리보기 표시: 배경 방식 유지 (현재 CSS 구조 그대로)
    if (preview) preview.style.backgroundImage = `url("${previewObjectUrl}")`;

    // (옵션) img 태그도 같이 쓰고 싶으면 아래로 바꾸면 됨:
    // if (previewImg) {
    //     previewImg.src = previewObjectUrl;
    //     previewImg.classList.remove("hidden");
    // }
});

/* ============================
   소개 글자수
============================ */

if (form?.bio && bioCount) {
    bioCount.textContent = String(form.bio.value.length);
    form.bio.addEventListener("input", () => {
        bioCount.textContent = String(form.bio.value.length);
    });
}

/* ============================
   Submit (Create Artist)
============================ */

form?.addEventListener("submit", async (e) => {
    e.preventDefault();

    const artistName = form.artistName.value.trim();
    if (!artistName) {
        alert("아티스트명 입력은 필수입니다.");
        form.artistName.focus();
        return;
    }

    const rawType = (form.artistType?.value || "").trim();
    const artistType = rawType ? rawType.toUpperCase() : null;

    const request = {
        artistName,
        country: form.country.value.trim() || null,
        artistType: artistType,
        debutDate: form.debutDate.value || null, // yyyy-MM-dd or null
        bio: form.bio.value.trim() || null,
    };

    // 마지막 방어
    if (selectedFile) {
        const ext = getExt(selectedFile.name);
        if (!IMAGE_ALLOWED_EXT.includes(ext)) {
            alert("유효하지 않은 이미지 파일 형식입니다. (jpg / jpeg / png만 가능)");
            resetImage();
            return;
        }
        if (selectedFile.size > IMAGE_MAX_SIZE) {
            alert("이미지 파일 용량이 너무 큽니다. (최대 5MB)");
            resetImage();
            return;
        }
        const ct = selectedFile.type || "";
        if (!ct.startsWith(IMAGE_ALLOWED_MIME_PREFIX)) {
            alert("유효하지 않은 이미지 파일입니다.");
            resetImage();
            return;
        }
    }

    const fd = new FormData();
    fd.append("request", new Blob([JSON.stringify(request)], { type: "application/json" }));
    if (selectedFile) fd.append("profileImage", selectedFile);

    submitBtn.disabled = true;
    const prevText = submitBtn.textContent;
    submitBtn.textContent = "등록 중...";

    try {
        const res = await authFetch("/api/admin/artists", {
            method: "POST",
            body: fd,
        });

        if (!res) return;

        const payload = await safeReadJson(res);

        if (!res.ok || payload?.success === false) {
            showApiError(res, payload);
            return;
        }

        alert(payload?.message || "아티스트가 생성되었습니다.");
        location.href = "/admin/artists";

    } catch (err) {
        console.error("아티스트 생성 오류:", err);
        alert("네트워크/서버 오류가 발생했습니다.");
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = prevText || "등록";
    }
});