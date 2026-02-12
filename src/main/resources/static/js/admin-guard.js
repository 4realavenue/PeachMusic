import { adminFetch } from "./admin.js";

document.addEventListener("DOMContentLoaded", async () => {

    try {
        // 관리자 권한 확인용 API
        const result = await adminFetch("/api/admin/ping", {
            method: "GET"
        });

        // adminFetch는 res.ok 아닐 때 alert 후 return undefined
        if (!result) {
            // 403일 가능성 → 홈으로 이동
            location.replace("/");
            return;
        }

        // 200이면 아무것도 안 함 (관리자 통과)

    } catch (e) {
        console.error("관리자 권한 확인 실패:", e);
        location.replace("/");
    }

});
