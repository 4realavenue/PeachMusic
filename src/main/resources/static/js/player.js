document.addEventListener("DOMContentLoaded", () => {

    /* =========================
       전역 오디오
    ========================= */

    const audio = document.querySelector(".player audio");

    if (!audio) {
        console.error("❌ 하단 플레이어 audio를 찾지 못함");
        return;
    }

    /* =========================
       DOM
    ========================= */

    const playBtn = document.getElementById("playBtn");
    const progressFill = document.getElementById("progressFill");
    const progressBar = document.getElementById("progressBar");
    const currentTimeEl = document.getElementById("currentTime");
    const durationEl = document.getElementById("duration");
    const playerTitle = document.getElementById("playerTitle");

    /* =========================
       ✅ Player Toggle
    ========================= */

    const player = document.querySelector(".player");
    const toggleBtn = document.getElementById("playerToggleBtn");

    toggleBtn?.addEventListener("click", () => {
        if (!player) return;

        player.classList.toggle("collapsed");

        // collapsed면 ▲, 펼치면 ▼
        if (player.classList.contains("collapsed")) {
            toggleBtn.textContent = "▲";
        } else {
            toggleBtn.textContent = "▼";
        }
    });

    /* =========================
       재생 버튼
    ========================= */
    playBtn?.addEventListener("click", () => {
        if (!audio.src) return;

        if (audio.paused) audio.play();
        else audio.pause();
    });

    /* =========================
       상세페이지에서 호출
    ========================= */
    window.playSongFromPage = function (url, title) {
        if (!url) return;

        const currentFile = audio.src.split("/").pop();
        const nextFile = url.split("/").pop();

        const isSame = currentFile === nextFile;

        if (isSame) {
            if (audio.paused) audio.play();
            else audio.pause();
            return;
        }

        audio.src = url;
        if (playerTitle) playerTitle.textContent = title || "Unknown";
        audio.play().catch(err => console.error("재생 실패:", err));

        // ✅ 곡을 새로 재생하면 자동으로 펼쳐주기
        if (player && toggleBtn && player.classList.contains("collapsed")) {
            player.classList.remove("collapsed");
            toggleBtn.textContent = "▼";
        }
    };

    /* =========================
       상태 동기화
    ========================= */
    audio.addEventListener("play", () => {
        if (playBtn) playBtn.textContent = "⏸";
    });

    audio.addEventListener("pause", () => {
        if (playBtn) playBtn.textContent = "▶";
    });

    /* =========================
       메타데이터 로드
    ========================= */
    audio.addEventListener("loadedmetadata", () => {
        if (durationEl) {
            durationEl.textContent = formatTime(audio.duration);
        }
    });

    /* =========================
       시간 업데이트
    ========================= */
    audio.addEventListener("timeupdate", () => {
        if (!audio.duration) return;

        const percent = (audio.currentTime / audio.duration) * 100;

        if (progressFill) {
            progressFill.style.width = percent + "%";
        }

        if (currentTimeEl) {
            currentTimeEl.textContent = formatTime(audio.currentTime);
        }
    });

    /* =========================
       프로그레스 클릭 이동
    ========================= */
    progressBar?.addEventListener("click", (e) => {
        if (!audio.duration) return;

        const rect = progressBar.getBoundingClientRect();
        const ratio = (e.clientX - rect.left) / rect.width;

        audio.currentTime = ratio * audio.duration;
    });

    /* =========================
       mm:ss 변환
    ========================= */
    function formatTime(sec) {
        if (!sec || isNaN(sec)) return "0:00";

        const m = Math.floor(sec / 60);
        const s = Math.floor(sec % 60);

        return `${m}:${s.toString().padStart(2, "0")}`;
    }

});