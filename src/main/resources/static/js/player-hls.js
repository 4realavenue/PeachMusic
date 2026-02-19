export function resolveAudioUrl(audioPath) {
    if (!audioPath) return null;
    if (audioPath.startsWith("http://") || audioPath.startsWith("https://")) return audioPath;
    if (audioPath.startsWith("/")) return audioPath;
    return `/${audioPath}`; // "storage/..." -> "/storage/..."
}

let hlsInstance = null;

function toKoreanPlaybackMessage(err) {
    // err가 hls.js error data일 수도 있고, 일반 Error일 수도 있음
    const msg = String(err?.message ?? err ?? "");

    // 브라우저 미지원/소스 미지원
    if (msg.includes("HLS not supported")) {
        return "이 브라우저에서는 스트리밍(m3u8) 재생을 지원하지 않습니다.";
    }
    if (msg.includes("NotSupportedError") || msg.includes("MEDIA_ERR_SRC_NOT_SUPPORTED")) {
        return "현재 브라우저에서 이 음원 형식을 재생할 수 없습니다.";
    }

    // 네트워크/서버 쪽
    if (msg.includes("manifest") || msg.includes("MANIFEST")) {
        return "스트리밍 목록(m3u8)을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }
    if (msg.includes("frag") || msg.includes("segment") || msg.includes(".ts")) {
        return "음원 조각 데이터를 불러오지 못했습니다. 네트워크 상태를 확인해 주세요.";
    }

    return "재생에 실패했습니다.";
}

export async function playHls(audioEl, url) {
    if (!url) throw new Error("No audio url");

    // Safari 등 네이티브 HLS 지원
    if (audioEl.canPlayType("application/vnd.apple.mpegurl")) {
        audioEl.src = url;
        await audioEl.play();
        return { ok: true };
    }

    // Chrome/Edge: hls.js
    if (window.Hls && window.Hls.isSupported()) {
        if (hlsInstance) {
            try { hlsInstance.destroy(); } catch {}
            hlsInstance = null;
        }

        hlsInstance = new window.Hls();

        return await new Promise((resolve, reject) => {
            hlsInstance.on(window.Hls.Events.ERROR, (_, data) => {
                // 치명적이면 실패 처리
                if (data?.fatal) reject(data);
            });

            hlsInstance.loadSource(url);
            hlsInstance.attachMedia(audioEl);

            hlsInstance.on(window.Hls.Events.MANIFEST_PARSED, async () => {
                try {
                    await audioEl.play();
                    resolve({ ok: true });
                } catch (e) {
                    reject(e);
                }
            });
        });
    }

    throw new Error("HLS not supported in this browser");
}

export function alertPlaybackError(err) {
    console.error("[playback error]", err);
    alert(toKoreanPlaybackMessage(err));
}
