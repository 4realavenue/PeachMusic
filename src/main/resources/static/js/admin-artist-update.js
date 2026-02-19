import { authFetch } from "/js/auth.js";

document.addEventListener("DOMContentLoaded", () => {
    const meta = document.getElementById("artistMeta");
    const artistId = meta?.dataset?.artistId;

    /* ===== 현재 값 토글 ===== */
    const btnToggleCurrent = document.getElementById("btnToggleCurrent");
    const currentBody = document.getElementById("currentBody");

    btnToggleCurrent?.addEventListener("click", () => {
        const opened = currentBody.classList.toggle("open");
        btnToggleCurrent.textContent = opened ? "현재 값 숨기기" : "현재 값 보기";
    });

    /* ===== 이미지 (서버 이미지 렌더링 X, 선택 시에만 표시) ===== */
    const fileInput = document.getElementById("profileImage");
    const btnPick = document.getElementById("btnPickImage");
    const btnReset = document.getElementById("btnResetImage");
    const fileHint = document.getElementById("fileHint");
    const imgBox = document.getElementById("imgBox");
    const imgPlaceholder = document.getElementById("imgPlaceholder");
    const btnSaveImage = document.getElementById("btnSaveImage");
    const imgHelp = document.getElementById("imgHelp");

    btnPick?.addEventListener("click", () => fileInput?.click());

    btnReset?.addEventListener("click", () => {
        if (fileInput) fileInput.value = "";
        if (fileHint) fileHint.textContent = "선택된 파일 없음";
        if (imgHelp) imgHelp.textContent = "";

        const img = document.getElementById("imgPreview");
        img?.remove();
        if (imgPlaceholder) imgPlaceholder.style.display = "";
    });

    fileInput?.addEventListener("change", () => {
        if (imgHelp) imgHelp.textContent = "";

        const file = fileInput.files?.[0];
        if (fileHint) fileHint.textContent = file ? file.name : "선택된 파일 없음";
        if (!file) return;

        const okTypes = ["image/jpeg", "image/jpg", "image/png"];
        if (!okTypes.includes(file.type)) {
            if (imgHelp) imgHelp.textContent = "jpg / jpeg / png 만 업로드 가능합니다.";
            fileInput.value = "";
            if (fileHint) fileHint.textContent = "선택된 파일 없음";
            return;
        }

        const url = URL.createObjectURL(file);

        let img = document.getElementById("imgPreview");
        if (!img) {
            img = document.createElement("img");
            img.id = "imgPreview";
            img.alt = "이미지 미리보기";
            imgBox.appendChild(img);
        }
        img.src = url;

        if (imgPlaceholder) imgPlaceholder.style.display = "none";
    });

    btnSaveImage?.addEventListener("click", async () => {
        if (imgHelp) imgHelp.textContent = "";

        const file = fileInput.files?.[0];
        if (!file) {
            if (imgHelp) imgHelp.textContent = "업로드할 이미지를 선택해주세요.";
            return;
        }

        const formData = new FormData();
        // 파라미터명: profileImage
        formData.append("profileImage", file);

        const res = await authFetch(`/api/admin/artists/${artistId}/image`, {
            method: "PATCH",
            body: formData,
        });
        if (!res) return;

        const data = await res.json().catch(() => null);
        if (!res.ok) {
            if (imgHelp) imgHelp.textContent = (data?.message ?? "프로필 이미지 수정 실패");
            return;
        }

        if (imgHelp) imgHelp.textContent = "이미지가 저장되었습니다.";
    });

    /* ===== 기본 정보 ===== */
    const artistNameEl = document.getElementById("artistName");
    const countryEl = document.getElementById("country");
    const artistTypeEl = document.getElementById("artistType");
    const debutDateEl = document.getElementById("debutDate");
    const bioEl = document.getElementById("bio");
    const bioCount = document.getElementById("bioCount");
    const btnSaveInfo = document.getElementById("btnSaveInfo");
    const btnCancelInfo = document.getElementById("btnCancelInfo");
    const infoHelp = document.getElementById("infoHelp");

    bioEl?.addEventListener("input", () => {
        if (bioCount) bioCount.textContent = String((bioEl.value ?? "").length);
    });

    btnCancelInfo?.addEventListener("click", () => {
        if (artistNameEl) artistNameEl.value = "";
        if (countryEl) countryEl.value = "";
        if (artistTypeEl) artistTypeEl.value = "";
        if (debutDateEl) debutDateEl.value = "";
        if (bioEl) {
            bioEl.value = "";
            if (bioCount) bioCount.textContent = "0";
        }
        if (infoHelp) infoHelp.textContent = "";
    });

    btnSaveInfo?.addEventListener("click", async () => {
        if (infoHelp) infoHelp.textContent = "";

        const payload = {};
        const artistName = (artistNameEl?.value ?? "").trim();
        const country = (countryEl?.value ?? "").trim();
        const artistType = (artistTypeEl?.value ?? "").trim();
        const debutDate = (debutDateEl?.value ?? "").trim();
        const bio = (bioEl?.value ?? "").trim();

        if (artistName) payload.artistName = artistName;
        if (country) payload.country = country;
        if (artistType) payload.artistType = artistType;
        if (debutDate) payload.debutDate = debutDate;
        if (bio) payload.bio = bio;

        if (Object.keys(payload).length === 0) {
            if (infoHelp) infoHelp.textContent = "수정할 값이 없습니다.";
            return;
        }

        const res = await authFetch(`/api/admin/artists/${artistId}`, {
            method: "PATCH",
            body: JSON.stringify(payload),
        });
        if (!res) return;

        const data = await res.json().catch(() => null);
        if (!res.ok) {
            if (infoHelp) infoHelp.textContent = (data?.message ?? "기본 정보 수정 실패");
            return;
        }

        if (infoHelp) infoHelp.textContent = "기본 정보가 저장되었습니다.";
    });
});
