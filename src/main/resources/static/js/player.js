import { playHls, alertPlaybackError, resolveAudioUrl } from "/js/player-hls.js";

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

    const prevBtn = document.getElementById("playerPrevBtn");
    const nextBtn = document.getElementById("playerNextBtn");

    /* =========================
       Player Toggle
    ========================= */
    const player = document.querySelector(".player");
    const toggleBtn = document.getElementById("playerToggleBtn");

    toggleBtn?.addEventListener("click", () => {
        if (!player) return;
        player.classList.toggle("collapsed");
        toggleBtn.textContent = player.classList.contains("collapsed") ? "▲" : "▼";
    });

    /* =========================
       ✅ 컨텍스트 큐
       - setPlayerQueue로 페이지가 “현재 리스트”를 넘김
       - ended 자동 다음곡
       - loop=true면 마지막 다음은 첫 곡, 첫 곡 prev는 마지막 곡
       - prev 5초 규칙: 5초 이상이면 현재곡 처음부터
    ========================= */
    let queue = []; // [{ songId, title, url? }]
    let currentIndex = -1;

    let loopEnabled = true;
    let contextKey = null;

    function updateNavButtons() {
        if (!prevBtn || !nextBtn) return;
        const hasQueue = queue.length > 0 && currentIndex >= 0;
        prevBtn.disabled = !hasQueue;
        nextBtn.disabled = !hasQueue;
    }

    function normalizeTracks(tracks) {
        return Array.isArray(tracks)
            ? tracks
                .map((t) => ({
                    songId: Number(t?.songId),
                    title: String(t?.title ?? "Unknown"),
                    url: t?.url ? resolveAudioUrl(t.url) : null, // 선택: url까지 넘기면 /play 재호출 감소
                }))
                .filter((t) => Number.isFinite(t.songId))
            : [];
    }

    function setQueueInternal(tracks = [], startSongId = null) {
        queue = normalizeTracks(tracks);

        if (queue.length === 0) {
            currentIndex = -1;
            updateNavButtons();
            return;
        }

        if (startSongId != null) {
            const sid = Number(startSongId);
            const idx = queue.findIndex((t) => t.songId === sid);
            currentIndex = idx >= 0 ? idx : 0;
        } else {
            currentIndex = 0;
        }

        updateNavButtons();
    }

    function findIndexBySongId(songId) {
        const sid = Number(songId);
        if (!Number.isFinite(sid)) return -1;
        return queue.findIndex((t) => t.songId === sid);
    }

    function getNextIndex() {
        if (queue.length === 0 || currentIndex < 0) return -1;
        if (currentIndex + 1 < queue.length) return currentIndex + 1;
        return loopEnabled ? 0 : -1;
    }

    function getPrevIndex() {
        if (queue.length === 0 || currentIndex < 0) return -1;
        if (currentIndex - 1 >= 0) return currentIndex - 1;
        return loopEnabled ? queue.length - 1 : -1;
    }

    /* =========================
       ✅ /play는 트랙 전환에서만 호출
       - prev/next/ended에서 사용
       - 페이지에서 클릭 재생은 “이미 url을 받아서” playSongFromPage로 들어오는게 베스트
    ========================= */
    async function fetchStreamingUrl(songId) {
        const res = await fetch(`/api/songs/${songId}/play`, { method: "GET" });

        let payload = null;
        try {
            payload = await res.json();
        } catch {
            payload = null;
        }

        if (!res.ok || payload?.success === false) {
            const msg = payload?.message;
            if (res.status === 400) alert(msg || "스트리밍 불가능한 음원입니다.");
            else if (res.status === 404) alert(msg || "음원이 존재하지 않습니다.");
            else alert(msg || "재생 요청에 실패했습니다.");
            return null;
        }

        return resolveAudioUrl(payload?.data?.streamingUrl ?? null);
    }

    async function playByIndex(nextIndex) {
        if (queue.length === 0) return;
        if (nextIndex < 0 || nextIndex >= queue.length) return;

        const item = queue[nextIndex];
        if (!item) return;

        // ✅ url을 큐에 같이 넣어준 경우 /play 호출 없이 사용 가능
        const url = item.url || (await fetchStreamingUrl(item.songId));
        if (!url) return;

        currentIndex = nextIndex;
        updateNavButtons();

        await playTrack(url, item.title, item.songId);
    }

    /* =========================
       전역 재생 버튼
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
       ✅ prev / next
       - prev: 5초 이상 재생 중이면 현재곡 처음부터
       - 아니면 이전곡 (loop면 wrap)
       - next: 다음곡 (loop면 wrap)
    ========================= */
    prevBtn?.addEventListener("click", async () => {
        if (queue.length === 0 || currentIndex < 0) return;

        if (!audio.paused && audio.currentTime >= 5) {
            audio.currentTime = 0;
            try {
                await audio.play();
            } catch (e) {
                alertPlaybackError(e);
            }
            return;
        }

        const prevIdx = getPrevIndex();
        if (prevIdx < 0) return;
        await playByIndex(prevIdx);
    });

    nextBtn?.addEventListener("click", async () => {
        if (queue.length === 0 || currentIndex < 0) return;
        const nextIdx = getNextIndex();
        if (nextIdx < 0) return;
        await playByIndex(nextIdx);
    });

    /* =========================
       ✅ 자동 다음 곡(ended)
    ========================= */
    audio.addEventListener("ended", async () => {
        if (queue.length === 0 || currentIndex < 0) return;
        const nextIdx = getNextIndex();
        if (nextIdx < 0) return;
        await playByIndex(nextIdx);
    });

    /* =========================
       ✅ 페이지에서 쓰는 "공식 API"
       1) setPlayerQueue(tracks, startSongId, { loop:true, contextKey:"..." })
       2) playSongFromPage(url, title, songId)  // 큐는 이미 세팅되어 있다는 전제
    ========================= */

    // ✅ 공식: 큐 세팅
    window.setPlayerQueue = function (tracks, startSongId = null, options = {}) {
        loopEnabled = options?.loop !== false; // 기본 true
        contextKey = options?.contextKey ?? null;

        setQueueInternal(tracks, startSongId);
    };

    // ✅ 공식: 재생 (여기서는 /play 재호출 금지)
    window.playSongFromPage = async function (url, title, songId) {
        const fixedUrl = resolveAudioUrl(url);
        if (!fixedUrl) return;

        // 큐에 있는 곡이면 인덱스 맞추기
        const idx = findIndexBySongId(songId);
        if (idx >= 0) {
            currentIndex = idx;
            updateNavButtons();
        }

        const currentFile = (audio.src || "").split("/").pop();
        const nextFile = String(fixedUrl).split("/").pop();
        const isSame = currentFile && nextFile && currentFile === nextFile;

        // 같은 곡이면 토글만
        if (isSame) {
            try {
                if (audio.paused) await audio.play();
                else audio.pause();
            } catch (e) {
                alertPlaybackError(e);
            }
            return;
        }

        await playTrack(fixedUrl, title, songId);
    };

    /* =========================
       ✅ 실제 재생
       - 여기서 /play 재호출 금지
    ========================= */
    async function playTrack(url, title, songId) {
        try {
            if (playerTitle) playerTitle.textContent = title || "Unknown";

            audio.pause();
            audio.currentTime = 0;

            await playHls(audio, url);

            // 자동 펼치기
            if (player && toggleBtn && player.classList.contains("collapsed")) {
                player.classList.remove("collapsed");
                toggleBtn.textContent = "▼";
            }
        } catch (e) {
            alertPlaybackError(e);
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

    audio.addEventListener("loadedmetadata", () => {
        if (durationEl) durationEl.textContent = formatTime(audio.duration);
    });

    audio.addEventListener("timeupdate", () => {
        if (!audio.duration) return;

        const percent = (audio.currentTime / audio.duration) * 100;
        if (progressFill) progressFill.style.width = percent + "%";
        if (currentTimeEl) currentTimeEl.textContent = formatTime(audio.currentTime);
    });

    progressBar?.addEventListener("click", (e) => {
        if (!audio.duration) return;

        const rect = progressBar.getBoundingClientRect();
        const ratio = (e.clientX - rect.left) / rect.width;
        audio.currentTime = ratio * audio.duration;
    });

    function formatTime(sec) {
        if (!sec || isNaN(sec)) return "0:00";
        const m = Math.floor(sec / 60);
        const s = Math.floor(sec % 60);
        return `${m}:${String(s).padStart(2, "0")}`;
    }

    updateNavButtons();
});