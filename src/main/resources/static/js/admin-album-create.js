import { authFetch } from "/js/auth.js";

const API_CREATE = "/api/admin/albums";
const API_ARTISTS = "/api/admin/artists"; // 관리자 아티스트 목록 API 재사용

const els = {
    albumName: document.getElementById("albumName"),
    albumReleaseDate: document.getElementById("albumReleaseDate"),
    albumImage: document.getElementById("albumImage"),

    previewImg: document.getElementById("previewImg"),
    previewEmpty: document.getElementById("previewEmpty"),

    resetBtn: document.getElementById("resetBtn"),
    cancelBtn: document.getElementById("cancelBtn"),
    submitBtn: document.getElementById("submitBtn"),

    artistWord: document.getElementById("artistWord"),
    artistSearchBtn: document.getElementById("artistSearchBtn"),
    artistResult: document.getElementById("artistResult"),

    selectedArtist: document.getElementById("selectedArtist"),
    selectedEmpty: document.getElementById("selectedEmpty"),
};

let selected = new Map(); // artistId -> {artistId, artistName}

/* -------------------------
   Helpers
-------------------------- */
function escapeHtml(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function setPreview(file) {
    if (!file) {
        els.previewImg.classList.add("hidden");
        els.previewEmpty.classList.remove("hidden");
        els.previewImg.src = "";
        return;
    }

    const url = URL.createObjectURL(file);
    els.previewImg.src = url;
    els.previewImg.classList.remove("hidden");
    els.previewEmpty.classList.add("hidden");
}

function renderSelected() {
    const arr = Array.from(selected.values());

    if (arr.length === 0) {
        els.selectedEmpty.classList.remove("hidden");
    } else {
        els.selectedEmpty.classList.add("hidden");
    }

    // selectedEmpty는 유지하고 그 외 칩은 갈아끼우기
    els.selectedArtist.querySelectorAll(".chip").forEach((n) => n.remove());

    arr.forEach((a) => {
        const chip = document.createElement("div");
        chip.className = "chip";
        chip.dataset.id = a.artistId;
        chip.innerHTML = `
      <span>${escapeHtml(a.artistName)}</span>
      <span class="x" title="삭제">×</span>
    `;
        els.selectedArtist.appendChild(chip);
    });
}

function renderResult(list) {
    els.artistResult.innerHTML = "";

    if (!list || list.length === 0) {
        els.artistResult.innerHTML = `<div class="empty-mini">검색 결과가 없습니다.</div>`;
        return;
    }

    list.forEach((a) => {
        const isDeleted = (a.isDeleted ?? a.deleted) === true;
        const disabled = isDeleted ? "true" : "false";

        const chip = document.createElement("div");
        chip.className = "chip";
        chip.dataset.id = a.artistId;
        chip.dataset.disabled = disabled;
        chip.innerHTML = `
      <span>${escapeHtml(a.artistName)}</span>
      ${isDeleted ? `<span style="font-size:12px;color:#ff2d55;font-weight:900;">(비활성)</span>` : ""}
    `;
        els.artistResult.appendChild(chip);
    });
}

/* -------------------------
   Artist Search
-------------------------- */
async function searchArtists() {
    const word = (els.artistWord.value || "").trim();

    const params = new URLSearchParams();
    if (word) params.set("word", word);

    // 첫 페이지 검색만: lastId 없이 (필요하면 나중에 "더보기" 붙이면 됨)
    const res = await authFetch(`${API_ARTISTS}?${params.toString()}`, { method: "GET" });
    if (!res) return;

    const json = await res.json();
    if (!res.ok || json?.success === false) {
        alert(json?.message || "아티스트 검색 실패");
        return;
    }

    const content = json.data?.content || [];
    renderResult(content);
}

/* -------------------------
   Submit
-------------------------- */
async function submitAlbum() {
    const albumName = (els.albumName.value || "").trim();
    const albumReleaseDate = els.albumReleaseDate.value;
    const file = els.albumImage.files?.[0];
    const artistIdList = Array.from(selected.keys()).map((v) => Number(v));

    if (!albumName) return alert("앨범명을 입력해주세요.");
    if (!albumReleaseDate) return alert("발매일을 입력해주세요.");
    if (!file) return alert("앨범 이미지는 필수입니다.");
    if (artistIdList.length < 1) return alert("참여 아티스트는 최소 1명 이상 선택해야 합니다.");

    const request = {
        albumName,
        albumReleaseDate,
        artistIdList,
    };

    const form = new FormData();
    form.append("request", new Blob([JSON.stringify(request)], { type: "application/json" }));
    form.append("albumImage", file);

    const res = await authFetch(API_CREATE, { method: "POST", body: form });
    if (!res) return;

    const json = await res.json();

    if (!res.ok || json?.success === false) {
        alert(json?.message || "앨범 생성 실패");
        return;
    }

    alert(json.message || "앨범이 생성되었습니다.");
    location.href = "/admin/albums";
}

/* -------------------------
   Events
-------------------------- */
document.addEventListener("DOMContentLoaded", () => {
    // 이미지 선택 -> 미리보기
    els.albumImage.addEventListener("change", (e) => {
        const file = e.target.files?.[0];
        setPreview(file);
    });

    // 초기화
    els.resetBtn.addEventListener("click", () => {
        els.albumName.value = "";
        els.albumReleaseDate.value = "";
        els.albumImage.value = "";
        selected.clear();
        setPreview(null);
        renderSelected();

        els.artistWord.value = "";
        els.artistResult.innerHTML = `<div class="empty-mini">검색어를 입력하고 검색하세요.</div>`;
    });

    // 취소
    els.cancelBtn.addEventListener("click", () => {
        location.href = "/admin/albums";
    });

    // 등록
    els.submitBtn.addEventListener("click", () => {
        submitAlbum();
    });

    // 아티스트 검색
    els.artistSearchBtn.addEventListener("click", () => searchArtists());
    els.artistWord.addEventListener("keydown", (e) => {
        if (e.key === "Enter") searchArtists();
    });

    // 검색 결과 클릭 -> 선택 추가 (비활성은 막음)
    els.artistResult.addEventListener("click", (e) => {
        const chip = e.target.closest(".chip");
        if (!chip) return;

        const disabled = chip.dataset.disabled === "true";
        if (disabled) {
            alert("비활성화된 아티스트는 선택할 수 없습니다.\n복구 후 다시 시도해주세요.");
            return;
        }

        const artistId = Number(chip.dataset.id);
        const name = chip.querySelector("span")?.textContent || "";

        if (selected.has(artistId)) {
            alert("이미 선택된 아티스트입니다.");
            return;
        }

        selected.set(artistId, { artistId, artistName: name });
        renderSelected();
    });

    // 선택된 칩 X 클릭 -> 제거
    els.selectedArtist.addEventListener("click", (e) => {
        const chip = e.target.closest(".chip");
        if (!chip) return;

        // x 눌렀을 때만 삭제되게
        const isX = e.target.classList.contains("x");
        if (!isX) return;

        const artistId = Number(chip.dataset.id);
        selected.delete(artistId);
        renderSelected();
    });

    // 초기 렌더
    setPreview(null);
    renderSelected();
});
