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
       ✅ Volume Popover + Keyboard + Hint(1회)
    ========================= */
    const volumeWrap = document.getElementById("volumeWrap");
    const volumeBtn = document.getElementById("volumeBtn");
    const volumePopover = document.getElementById("volumePopover");
    const volumeRange = document.getElementById("volumeRange");
    const volumeHint = document.getElementById("volumeHint");

    const VOL_KEY = "peach_player_volume";   // 0~1
    const MUTE_KEY = "peach_player_muted";   // "1" | "0"
    const HINT_KEY = "peach_player_volume_hint_seen"; // "1"

    const clamp = (n, min, max) => Math.min(max, Math.max(min, n));

    function updateVolumeIcon() {
        if (!volumeBtn) return;

        if (audio.muted || audio.volume === 0) {
            volumeBtn.textContent = "🔇";
            volumeBtn.setAttribute("aria-label", "음소거 해제");
            return;
        }
        if (audio.volume < 0.5) {
            volumeBtn.textContent = "🔈";
            volumeBtn.setAttribute("aria-label", "음소거");
            return;
        }
        volumeBtn.textContent = "🔊";
        volumeBtn.setAttribute("aria-label", "음소거");
    }

    function saveVolume() {
        localStorage.setItem(VOL_KEY, String(audio.volume));
        localStorage.setItem(MUTE_KEY, audio.muted ? "1" : "0");
    }

    function loadVolume() {
        const v = Number(localStorage.getItem(VOL_KEY));
        const muted = localStorage.getItem(MUTE_KEY) === "1";
        const vol = Number.isFinite(v) ? clamp(v, 0, 1) : 0.8;

        audio.volume = vol;
        audio.muted = muted;

        if (volumeRange) volumeRange.value = String(Math.round(vol * 100));
        updateVolumeIcon();
    }

    function setPopover(open) {
        if (!volumePopover || !volumeBtn) return;

        volumePopover.classList.toggle("open", open);
        volumePopover.setAttribute("aria-hidden", open ? "false" : "true");
        volumeBtn.setAttribute("aria-expanded", open ? "true" : "false");

        if (open) {
            // 열리면 슬라이더 포커스 → 키보드 조절 바로 가능
            volumeRange?.focus();

            // ✅ 힌트는 "처음 1회만" 잠깐 보여주고 자동 숨김
            const seen = localStorage.getItem(HINT_KEY) === "1";
            if (volumeHint) {
                if (!seen) {
                    volumeHint.classList.remove("hidden");
                    localStorage.setItem(HINT_KEY, "1");
                    setTimeout(() => volumeHint.classList.add("hidden"), 2500);
                } else {
                    volumeHint.classList.add("hidden");
                }
            }
        }
    }

    function togglePopover() {
        const isOpen = volumePopover?.classList.contains("open");
        setPopover(!isOpen);
    }

    function stepVolume(delta) {
        const next = clamp(audio.volume + delta, 0, 1);
        audio.volume = next;
        if (audio.muted && next > 0) audio.muted = false;

        if (volumeRange) volumeRange.value = String(Math.round(next * 100));
        updateVolumeIcon();
        saveVolume();
    }

    function isVolumeFocused() {
        const a = document.activeElement;
        return (
            a === volumeBtn ||
            a === volumeRange ||
            (volumePopover && a instanceof Node && volumePopover.contains(a))
        );
    }

    volumeBtn?.addEventListener("click", (e) => {
        e.stopPropagation();
        togglePopover();
    });

    // 바깥 클릭 → 닫기
    document.addEventListener("click", (e) => {
        if (!volumePopover?.classList.contains("open")) return;
        const t = e.target;
        if (volumeWrap && t instanceof Node && volumeWrap.contains(t)) return;
        setPopover(false);
    });

    // ESC → 닫기
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && volumePopover?.classList.contains("open")) {
            setPopover(false);
            volumeBtn?.focus();
        }
    });

    // 슬라이더 입력
    volumeRange?.addEventListener("input", () => {
        const v = clamp(Number(volumeRange.value) / 100, 0, 1);
        audio.volume = v;
        if (audio.muted && v > 0) audio.muted = false;
        updateVolumeIcon();
        saveVolume();
    });

    // 키보드 조절(볼륨 영역 포커스일 때만)
    document.addEventListener("keydown", (e) => {
        if (!isVolumeFocused()) return;

        if (e.key === "ArrowLeft" || e.key === "ArrowDown") {
            e.preventDefault();
            stepVolume(-0.05);
        } else if (e.key === "ArrowRight" || e.key === "ArrowUp") {
            e.preventDefault();
            stepVolume(+0.05);
        } else if (e.key === "m" || e.key === "M") {
            e.preventDefault();
            audio.muted = !audio.muted;
            updateVolumeIcon();
            saveVolume();
        }
    });

    audio.addEventListener("volumechange", () => {
        if (volumeRange) volumeRange.value = String(Math.round(clamp(audio.volume, 0, 1) * 100));
        updateVolumeIcon();
    });

    loadVolume();

    /* =========================
       ✅ 컨텍스트 큐
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
                    url: t?.url ? resolveAudioUrl(t.url) : null, // url 포함 시 /play 재호출 감소
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
    ========================= */
    prevBtn?.addEventListener("click", async () => {
        if (queue.length === 0 || currentIndex < 0) return;

        // 5초 규칙
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
    ========================= */
    window.setPlayerQueue = function (tracks, startSongId = null, options = {}) {
        loopEnabled = options?.loop !== false; // 기본 true
        contextKey = options?.contextKey ?? null;
        setQueueInternal(tracks, startSongId);
    };

    window.playSongFromPage = async function (url, title, songId) {
        const fixedUrl = resolveAudioUrl(url);
        if (!fixedUrl) return;

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