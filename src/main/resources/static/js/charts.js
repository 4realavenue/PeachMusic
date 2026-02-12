document.addEventListener("DOMContentLoaded", () => {
    const listEl = document.getElementById("chartList");
    const errorBox = document.getElementById("errorBox");

    const prevBtn = document.getElementById("prevBtn");
    const nextBtn = document.getElementById("nextBtn");
    const pageLabel = document.getElementById("pageLabel");

    const PAGE_SIZE = 10;

    let allItems = []; // ✅ Top100 전체 저장
    let page = 0;      // ✅ 0-based

    prevBtn.addEventListener("click", () => {
        if (page === 0) return;
        page--;
        renderPage();
    });

    nextBtn.addEventListener("click", () => {
        if (page >= getTotalPages() - 1) return;
        page++;
        renderPage();
    });

    loadTop100();

    async function loadTop100() {
        hideError();
        listEl.innerHTML = "";

        try {
            // ✅ 서버에서 100개 리스트 한번에 받기
            const res = await fetch("/api/songs/ranking/Top100");
            const payload = await res.json();

            if (!res.ok || payload?.success === false) {
                showError(payload?.message ?? "인기차트 조회 실패");
                return;
            }

            // ✅ payload.data = [{title, score, id}, ...] (100개)
            allItems = Array.isArray(payload.data) ? payload.data : [];

            if (allItems.length === 0) {
                showError("차트 데이터가 없습니다.");
                return;
            }

            page = 0;
            renderPage();

        } catch (e) {
            console.error(e);
            showError("서버 오류가 발생했습니다.");
        }
    }

    function renderPage() {
        listEl.innerHTML = "";

        const totalPages = getTotalPages();
        const start = page * PAGE_SIZE;     // 0, 10, 20...
        const end = start + PAGE_SIZE;      // 10, 20, 30...

        const pageItems = allItems.slice(start, end);

        // ✅ 버튼/라벨 상태
        pageLabel.textContent = `${page + 1} / ${totalPages}`;
        prevBtn.disabled = page === 0;
        nextBtn.disabled = page === totalPages - 1;

        // ✅ 렌더링: 순위는 1~100 고정으로 보이게 계산
        pageItems.forEach((row, idx) => {
            const rank = start + idx + 1;  // 1~10, 11~20, 21~30...
            const title = row.title ?? "-";
            const songId = row.id;

            const div = document.createElement("div");
            div.className = "chart-row";

            div.innerHTML = `
        <div class="rank">${rank}</div>
        <div class="title">${escapeHtml(title)}</div>
        <button class="play-btn" type="button">재생</button>
      `;

            // 제목 클릭 -> 곡 상세 (원하는 라우팅으로 바꿔도 됨)
            div.querySelector(".title").addEventListener("click", () => {
                location.href = `/songs/${songId}`;
            });

            // 재생 버튼 -> 나중에 플레이어 연동
            div.querySelector(".play-btn").addEventListener("click", () => {
                alert(`재생: ${title} (songId=${songId})`);
            });

            listEl.appendChild(div);
        });
    }

    function getTotalPages() {
        return Math.max(1, Math.ceil(allItems.length / PAGE_SIZE));
    }

    function showError(msg) {
        errorBox.classList.remove("hidden");
        errorBox.textContent = msg;
    }

    function hideError() {
        errorBox.classList.add("hidden");
        errorBox.textContent = "";
    }

    function escapeHtml(str) {
        return String(str)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
});