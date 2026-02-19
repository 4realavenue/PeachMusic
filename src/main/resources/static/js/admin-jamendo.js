import { authFetch } from "/js/auth.js";

const API_URL = "/api/admin/openapi/jamendo";

const els = {
    startDate: document.getElementById("startDate"),
    endDate: document.getElementById("endDate"),
    loadBtn: document.getElementById("loadBtn"),
    lastRange: document.getElementById("lastRange"),
    lastStatus: document.getElementById("lastStatus"),
};

/* ============================
   날짜 유틸
============================ */
function getTodayString() {
    const d = new Date();
    return d.toISOString().slice(0, 10);
}

function isFuture(dateStr) {
    return dateStr > getTodayString();
}

/* ============================
   버튼 상태 제어
============================ */
function setLoading(isLoading) {
    els.loadBtn.disabled = isLoading;
    els.loadBtn.textContent = isLoading ? "적재 중..." : "데이터 초기 적재";
}

/* ============================
   실행
============================ */
async function executeLoad() {

    const start = els.startDate.value;
    const end = els.endDate.value;

    if (!start || !end) return;
    if (start > end) return;
    if (isFuture(start) || isFuture(end)) return;

    try {
        setLoading(true);

        // 🔥 시작 시간 기록
        const startTime = performance.now();

        const res = await authFetch(API_URL, {
            method: "POST",
            body: JSON.stringify({
                startDate: start,
                endDate: end
            })
        });

        if (!res) return;

        const json = await res.json();

        // 🔥 종료 시간 기록
        const endTime = performance.now();
        const durationMs = Math.round(endTime - startTime);
        const durationSec = (durationMs / 1000).toFixed(2);

        // 요청 기간은 항상 표시
        els.lastRange.textContent = `${start} ~ ${end}`;

        if (!res.ok || json.success === false) {
            els.lastStatus.textContent = `실패 (소요시간: ${durationSec}초)`;
            return;
        }

        // ✅ 성공 시 시간 포함
        els.lastStatus.textContent = `성공 (소요시간: ${durationSec}초)`;

    } catch (e) {
        console.error(e);
        els.lastStatus.textContent = "오류";
    } finally {
        setLoading(false);
    }
}

/* ============================
   Init
============================ */
document.addEventListener("DOMContentLoaded", () => {

    const today = getTodayString();

    // 미래 날짜 선택 차단
    els.startDate.max = today;
    els.endDate.max = today;

    els.loadBtn.addEventListener("click", executeLoad);
});
