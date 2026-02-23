import { authFetch, removeToken } from "./auth.js";

document.addEventListener("DOMContentLoaded", () => {

    const userNicknameEl = document.getElementById("userNickname");
    const userEmailEl = document.getElementById("userEmail");

    const editBtn = document.getElementById("editBtn");
    const withdrawBtn = document.getElementById("withdrawBtn");

    const actionArea = document.getElementById("actionArea");
    const editForm = document.getElementById("editForm");
    const withdrawInfo = document.getElementById("withdrawInfo");

    const cancelEditBtn = document.getElementById("cancelEditBtn");
    const cancelWithdrawBtn = document.getElementById("cancelWithdrawBtn");

    const saveBtn = document.getElementById("saveBtn");
    const confirmWithdrawBtn = document.getElementById("confirmWithdrawBtn");

    const editName = document.getElementById("editName");
    const editNickname = document.getElementById("editNickname");

    let currentUser = null;

    init();

    async function init() {
        await loadUser();
    }

    async function loadUser() {

        const res = await authFetch("/api/users");
        const result = await res.json();

        if (!result.success) return;

        currentUser = result.data;

        userNicknameEl.textContent = currentUser.nickname;
        userEmailEl.textContent = currentUser.email;
    }

    /* =========================
       하단 영역 숨기기
    ========================== */
    function hideAll() {
        actionArea.classList.add("hidden");
        editForm.classList.add("hidden");
        withdrawInfo.classList.add("hidden");
    }

    /* =========================
       정보 수정 클릭
    ========================== */
    editBtn.addEventListener("click", () => {

        editName.value = currentUser.name;
        editNickname.value = currentUser.nickname;

        actionArea.classList.remove("hidden");
        editForm.classList.remove("hidden");
        withdrawInfo.classList.add("hidden");
    });

    cancelEditBtn.addEventListener("click", hideAll);

    saveBtn.addEventListener("click", async () => {

        const body = {};

        if (editName.value !== currentUser.name) {
            body.name = editName.value;
        }

        if (editNickname.value !== currentUser.nickname) {
            body.nickname = editNickname.value;
        }

        if (Object.keys(body).length === 0) {
            hideAll();
            return;
        }

        const res = await authFetch("/api/users", {
            method: "PATCH",
            body: JSON.stringify(body)
        });

        const result = await res.json();

        if (!result.success) {
            alert("수정 실패");
            return;
        }

        alert("수정 완료");

        await loadUser();
        hideAll();
    });

    /* =========================
       회원 탈퇴 클릭
    ========================== */
    withdrawBtn.addEventListener("click", () => {

        actionArea.classList.remove("hidden");
        withdrawInfo.classList.remove("hidden");
        editForm.classList.add("hidden");
    });

    cancelWithdrawBtn.addEventListener("click", hideAll);

    confirmWithdrawBtn.addEventListener("click", async () => {

        const confirmDelete = confirm("정말 탈퇴하시겠습니까?");
        if (!confirmDelete) return;

        const res = await authFetch("/api/users", {
            method: "DELETE"
        });

        if (res.ok) {
            alert("탈퇴 완료");
            removeToken();
            location.href = "/";
        }
    });

});
