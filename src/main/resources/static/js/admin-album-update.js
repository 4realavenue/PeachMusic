// /js/admin-album-update.js
import { authFetch } from "/js/auth.js";

document.addEventListener("DOMContentLoaded", () => {
    const meta = document.getElementById("albumMeta");
    const albumId = meta?.dataset?.albumId;

    /* ===== 현재 값 토글 ===== */
    const btnToggleCurrent = document.getElementById("btnToggleCurrent");
    const currentBody = document.getElementById("currentBody");
    btnToggleCurrent?.addEventListener("click", () => {
        const isHidden = currentBody.classList.toggle("hidden");
        btnToggleCurrent.textContent = isHidden ? "현재 값 보기" : "현재 값 숨기기";
    });

    /* ===== 이미지 (선택 시에만 표시) ===== */
    const fileInput = document.getElementById("albumImage");
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
        formData.append("albumImage", file);

        const res = await authFetch(`/api/admin/albums/${albumId}/image`, {
            method: "PATCH",
            body: formData,
        });
        if (!res) return;

        const data = await res.json().catch(() => null);
        if (!res.ok) {
            if (imgHelp) imgHelp.textContent = (data?.message ?? "앨범 이미지 수정 실패");
            return;
        }

        if (imgHelp) imgHelp.textContent = "이미지가 저장되었습니다.";
    });

    /* ===== 기본 정보 ===== */
    const albumNameEl = document.getElementById("albumName");
    const albumReleaseDateEl = document.getElementById("albumReleaseDate");
    const btnSaveInfo = document.getElementById("btnSaveInfo");
    const btnCancelInfo = document.getElementById("btnCancelInfo");
    const infoHelp = document.getElementById("infoHelp");

    btnCancelInfo?.addEventListener("click", () => {
        if (albumNameEl) albumNameEl.value = "";
        if (albumReleaseDateEl) albumReleaseDateEl.value = "";
        if (infoHelp) infoHelp.textContent = "";
    });

    btnSaveInfo?.addEventListener("click", async () => {
        if (infoHelp) infoHelp.textContent = "";

        const payload = {};
        const albumName = (albumNameEl?.value ?? "").trim();
        const albumReleaseDate = (albumReleaseDateEl?.value ?? "").trim();

        if (albumName) payload.albumName = albumName;
        if (albumReleaseDate) payload.albumReleaseDate = albumReleaseDate;

        if (Object.keys(payload).length === 0) {
            if (infoHelp) infoHelp.textContent = "수정할 값이 없습니다.";
            return;
        }

        const res = await authFetch(`/api/admin/albums/${albumId}`, {
            method: "PATCH",
            body: JSON.stringify(payload),
        });
        if (!res) return;

        const data = await res.json().catch(() => null);
        if (!res.ok) {
            if (infoHelp) infoHelp.textContent = (data?.message ?? "앨범 기본 정보 수정 실패");
            return;
        }

        if (infoHelp) infoHelp.textContent = "기본 정보가 저장되었습니다.";
    });

    /* ===== 참여 아티스트 ===== */
    const initialArtistsEl = document.getElementById("initialArtists");
    const artistSearchInput = document.getElementById("artistSearchInput");
    const btnSearchArtist = document.getElementById("btnSearchArtist");
    const artistSearchResult = document.getElementById("artistSearchResult");
    const selectedArtistsBox = document.getElementById("selectedArtists");
    const selectedHint = document.getElementById("selectedHint");
    const btnSaveArtists = document.getElementById("btnSaveArtists");
    const artistHelp = document.getElementById("artistHelp");

    const selectedMap = new Map(); // key: artistId, val: { artistId, artistName }

    try {
        const raw = initialArtistsEl?.dataset?.initial;
        if (raw) {
            const arr = JSON.parse(raw);
            (arr ?? []).forEach(a => {
                if (!a) return;
                selectedMap.set(String(a.artistId), { artistId: a.artistId, artistName: a.artistName });
            });
        }
    } catch (e) {
        console.warn("initialArtists parse fail", e);
    }

    renderSelectedArtists();

    btnSearchArtist?.addEventListener("click", () => doSearch());
    artistSearchInput?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            doSearch();
        }
    });

    async function doSearch() {
        if (artistHelp) artistHelp.textContent = "";

        const word = (artistSearchInput?.value ?? "").trim();
        if (!word) {
            if (artistSearchResult) artistSearchResult.innerHTML = `<div class="panel-empty">검색어를 입력하고 검색하세요.</div>`;
            return;
        }

        const res = await authFetch(`/api/admin/artists?word=${encodeURIComponent(word)}`, { method: "GET" });
        if (!res) return;

        const data = await res.json().catch(() => null);
        if (!res.ok) {
            if (artistSearchResult) artistSearchResult.innerHTML = `<div class="panel-empty">${data?.message ?? "검색 실패"}</div>`;
            return;
        }

        const list = data?.data?.content ?? [];
        if (list.length === 0) {
            if (artistSearchResult) artistSearchResult.innerHTML = `<div class="panel-empty">검색 결과가 없습니다.</div>`;
            return;
        }

        if (artistSearchResult) {
            artistSearchResult.innerHTML = list.map(a => {
                const id = a.artistId;
                const name = a.artistName ?? "-";
                const isDeleted = a.isDeleted === true;

                const btnText = isDeleted ? "비활성" : (selectedMap.has(String(id)) ? "추가됨" : "추가");
                const disabled = (isDeleted || selectedMap.has(String(id))) ? "disabled" : "";

                return `
          <div class="artist-row">
            <div class="artist-name">${escapeHtml(name)}</div>
            <button class="chip-btn ${isDeleted ? "muted" : ""}" data-add-id="${id}" ${disabled}>
              ${btnText}
            </button>
          </div>
        `;
            }).join("");

            artistSearchResult.querySelectorAll("[data-add-id]").forEach(btn => {
                btn.addEventListener("click", () => {
                    const id = btn.getAttribute("data-add-id");
                    const row = btn.closest(".artist-row");
                    const name = row?.querySelector(".artist-name")?.textContent ?? "-";
                    if (selectedMap.has(String(id))) return;

                    selectedMap.set(String(id), { artistId: Number(id), artistName: name });
                    renderSelectedArtists();

                    btn.textContent = "추가됨";
                    btn.setAttribute("disabled", "disabled");
                });
            });
        }
    }

    function renderSelectedArtists() {
        const arr = Array.from(selectedMap.values());

        if (arr.length === 0) {
            if (selectedArtistsBox) selectedArtistsBox.innerHTML = `<div class="panel-empty">선택된 아티스트가 없습니다.</div>`;
            if (selectedHint) selectedHint.textContent = "최소 1명 이상 선택해야 합니다.";
            return;
        }

        if (selectedHint) selectedHint.textContent = `${arr.length}명 선택됨`;

        if (selectedArtistsBox) {
            selectedArtistsBox.innerHTML = arr.map(a => `
        <div class="artist-row">
          <div class="artist-name">${escapeHtml(a.artistName)}</div>
          <button class="chip-btn danger" data-remove-id="${a.artistId}">삭제</button>
        </div>
      `).join("");

            selectedArtistsBox.querySelectorAll("[data-remove-id]").forEach(btn => {
                btn.addEventListener("click", () => {
                    const id = btn.getAttribute("data-remove-id");
                    selectedMap.delete(String(id));
                    renderSelectedArtists();
                });
            });
        }
    }

    btnSaveArtists?.addEventListener("click", async () => {
        if (artistHelp) artistHelp.textContent = "";

        const artistIdList = Array.from(selectedMap.values()).map(a => a.artistId);
        if (artistIdList.length === 0) {
            if (artistHelp) artistHelp.textContent = "참여 아티스트는 최소 1명 이상이어야 합니다.";
            return;
        }

        const res = await authFetch(`/api/admin/albums/${albumId}/artists`, {
            method: "PUT",
            body: JSON.stringify({ artistIdList }),
        });
        if (!res) return;

        const data = await res.json().catch(() => null);
        if (!res.ok) {
            if (artistHelp) artistHelp.textContent = (data?.message ?? "참여 아티스트 저장 실패");
            return;
        }

        if (artistHelp) artistHelp.textContent = "참여 아티스트 목록이 저장되었습니다.";
    });

    function escapeHtml(str) {
        return String(str)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
});
