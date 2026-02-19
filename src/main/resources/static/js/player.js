import { playHls, alertPlaybackError } from "/js/player-hls.js";

document.addEventListener("DOMContentLoaded", () => {

    /* =========================
       전역 오디오
    ========================= */
    const audio = document.querySelector(".player audio") || document.getElementById("audioPlayer");
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

    // ✅ prev/next (ID 충돌 방지 버전)
    const prevBtn = document.getElementById("playerPrevBtn");
    const nextBtn = document.getElementById("playerNextBtn");

    /* =========================
       ✅ Player Toggle
    ========================= */
    const player = document.querySelector(".player");
    const toggleBtn = document.getElementById("playerToggleBtn");

    toggleBtn?.addEventListener("click", () => {
        if (!player) return;
        player.classList.toggle("collapsed");
        toggleBtn.textContent = player.classList.contains("collapsed") ? "▲" : "▼";
    });

    /* =========================
       ✅ 재생수 중복 증가 방지(전역 플레이어 기준 곡당 1회)
    ========================= */
    const playedOnce = new Set();

    /* =========================
       ✅ 전역 큐(세션용)
       - 사용자가 클릭해서 재생한 곡들을 순서대로 쌓음
       - prev/next는 이 큐 기준으로 이동
    ========================= */
    const queue = [];            // [{songId, title}]
    let currentIndex = -1;

    function updateNavButtons() {
        if (!prevBtn || !nextBtn) return;
        prevBtn.disabled = currentIndex <= 0;
        nextBtn.disabled = currentIndex < 0 || currentIndex >= queue.length - 1;
    }

    function ensureInQueue(songId, title) {
        const sid = Number(songId);
        if (!Number.isFinite(sid)) return;

        // 현재 곡이 큐의 현재Index와 같으면 패스
        if (currentIndex >= 0 && queue[currentIndex]?.songId === sid) {
            updateNavButtons();
            return;
        }

        // "현재Index 이후"는 앞으로 가기 스택이므로 잘라냄 (브라우저 히스토리처럼)
        if (currentIndex >= 0 && currentIndex < queue.length - 1) {
            queue.splice(currentIndex + 1);
        }

        // 같은 곡이 뒤에 중복으로 계속 쌓이지 않게: 마지막이 같은 곡이면 교체
        if (queue.length > 0 && queue[queue.length - 1]?.songId === sid) {
            queue[queue.length - 1] = { songId: sid, title: title || "Unknown" };
        } else {
            queue.push({ songId: sid, title: title || "Unknown" });
        }

        currentIndex = queue.length - 1;
        updateNavButtons();
    }

    async function fetchStreamingUrl(songId) {
        const res = await fetch(`/api/songs/${songId}/play`, { method: "GET" });
        let payload = null;
        try { payload = await res.json(); } catch { payload = null; }

        if (!res.ok || payload?.success === false) {
            const msg = payload?.message;
            if (res.status === 400) alert(msg || "스트리밍 불가능한 음원입니다.");
            else if (res.status === 404) alert(msg || "음원이 존재하지 않습니다.");
            else alert(msg || "재생 요청에 실패했습니다.");
            return null;
        }

        return payload?.data?.streamingUrl ?? null;
    }

    async function playByIndex(nextIndex) {
        if (nextIndex < 0 || nextIndex >= queue.length) return;
        const item = queue[nextIndex];
        if (!item) return;

        const url = await fetchStreamingUrl(item.songId);
        if (!url) return;

        // 인덱스를 먼저 확정(실패하면 다시 돌려도 되지만 UX상 여기서 고정이 낫다)
        currentIndex = nextIndex;
        updateNavButtons();

        // 실제 재생
        await playTrack(url, item.title, item.songId, { skipQueue: true });
    }

    /* =========================
       재생 버튼(전역)
    ========================= */
    playBtn?.addEventListener("click", async () => {
        if (!audio.src) return;
        try {
            if (audio.paused) await audio.play();
            else audio.pause();
        } catch (e) {
            alertPlaybackError(e);
        }
    });

    /* =========================
       ✅ 이전/다음 버튼
    ========================= */
    prevBtn?.addEventListener("click", async () => {
        if (currentIndex <= 0) return;
        await playByIndex(currentIndex - 1);
    });

    nextBtn?.addEventListener("click", async () => {
        if (currentIndex < 0 || currentIndex >= queue.length - 1) return;
        await playByIndex(currentIndex + 1);
    });

    /* =========================
       ✅ 상세/리스트 페이지에서 호출 (전역 재생)
       - window.playSongFromPage(url, title, songId)
       - HLS 재생 + 재생 성공 후 재생수 증가(400/404 처리)
       - ✅ 큐에도 자동 반영
    ========================= */
    window.playSongFromPage = async function (url, title, songId) {
        if (!url) return;

        const currentFile = (audio.src || "").split("/").pop();
        const nextFile = String(url).split("/").pop();
        const isSame = currentFile && nextFile && currentFile === nextFile;

        // 같은 곡이면 토글만 (재생수 추가 X)
        if (isSame) {
            try {
                if (audio.paused) await audio.play();
                else audio.pause();
            } catch (e) {
                alertPlaybackError(e);
            }
            return;
        }

        await playTrack(url, title, songId, { skipQueue: false });
    };

    /**
     * ✅ 내부 실제 재생 함수
     * options.skipQueue=true 이면 큐 추가/정리 안 함(Prev/Next에서 호출할 때)
     */
    async function playTrack(url, title, songId, options = { skipQueue: false }) {
        try {
            if (playerTitle) playerTitle.textContent = title || "Unknown";

            // 기존 재생 정리
            audio.pause();
            audio.currentTime = 0;

            // ✅ 1) HLS 재생
            await playHls(audio, url);

            // ✅ 2) 재생 성공 후 재생수 1회 증가
            if (songId != null) {
                await increasePlayCountOnce(Number(songId));
            }

            // ✅ 3) 큐 반영
            if (!options?.skipQueue && songId != null) {
                ensureInQueue(Number(songId), title || "Unknown");
            } else {
                updateNavButtons();
            }

            // ✅ 4) 곡 새로 재생하면 자동 펼치기
            if (player && toggleBtn && player.classList.contains("collapsed")) {
                player.classList.remove("collapsed");
                toggleBtn.textContent = "▼";
            }
        } catch (e) {
            alertPlaybackError(e);
        }
    }

    /* =========================
       ✅ 재생수 증가 (곡당 1회)
    ========================= */
    async function increasePlayCountOnce(songId) {
        if (playedOnce.has(songId)) return;

        playedOnce.add(songId);

        let res = null;
        let payload = null;

        try {
            res = await fetch(`/api/songs/${songId}/play`, { method: "GET" });
            try { payload = await res.json(); } catch { payload = null; }

            if (!res.ok || payload?.success === false) {
                const msg = payload?.message;

                if (res.status === 400) alert(msg || "스트리밍 불가능한 음원입니다.");
                else if (res.status === 404) alert(msg || "음원이 존재하지 않습니다.");
                else alert(msg || "재생 횟수 반영에 실패했습니다.");

                // 실패면 다음에 다시 시도 가능하도록 롤백
                playedOnce.delete(songId);
            }
        } catch (e) {
            console.error(e);
            alert("재생 횟수 반영 중 서버 오류가 발생했습니다.");
            playedOnce.delete(songId);
        }
    }

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
        if (durationEl) durationEl.textContent = formatTime(audio.duration);
    });

    /* =========================
       시간 업데이트
    ========================= */
    audio.addEventListener("timeupdate", () => {
        if (!audio.duration) return;

        const percent = (audio.currentTime / audio.duration) * 100;
        if (progressFill) progressFill.style.width = percent + "%";
        if (currentTimeEl) currentTimeEl.textContent = formatTime(audio.currentTime);
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
        return `${m}:${String(s).padStart(2, "0")}`;
    }

    // 초기 상태
    updateNavButtons();
});