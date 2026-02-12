import { getToken } from "/js/auth.js";

document.addEventListener("DOMContentLoaded", () => {

    const token = getToken();

    if (!token) {
        const goLogin = confirm("로그인이 필요합니다.\n로그인 페이지로 이동하시겠습니까?");
        if (goLogin) location.href = "/login";
        else location.href = "/";
        return;
    }

    loadPlaylists();

    const modal = document.getElementById("createModal");

    document.getElementById("createPlaylistBtn")
        .addEventListener("click", () => {
            modal.classList.remove("hidden");
        });

    document.getElementById("cancelCreateBtn")
        .addEventListener("click", () => {
            modal.classList.add("hidden");
        });

    document.getElementById("confirmCreateBtn")
        .addEventListener("click", createPlaylist);
});


/* ================================
   플레이리스트 목록 조회
================================ */
async function loadPlaylists() {

    try {
        const res = await fetch("/api/playlists", {
            headers: { "Authorization": getToken() }
        });

        if (res.status === 401) {
            location.href = "/login";
            return;
        }

        const data = await res.json();

        if (!data.success) {
            console.error("조회 실패:", data.message);
            return;
        }

        renderPlaylists(data.data);

    } catch (err) {
        console.error("조회 에러:", err);
    }
}


/* ================================
   플레이리스트 렌더링
================================ */
function renderPlaylists(list) {

    const grid = document.getElementById("playlistGrid");
    const emptyMessage = document.getElementById("emptyMessage");

    grid.innerHTML = "";

    if (!list || list.length === 0) {
        emptyMessage.style.display = "block";
        return;
    }

    emptyMessage.style.display = "none";

    list.forEach(p => {

        const item = document.createElement("div");
        item.className = "playlist-item";

        item.innerHTML = `
            <div class="playlist-box"
                 style="
                    background-image: url('${p.playlistImage || ""}');
                    background-size: cover;
                    background-position: center;
                 ">
            </div>
            <div class="playlist-label">
                ${p.playlistName}
            </div>
        `;

        // 🔥 여기 추가 (중요)
        item.addEventListener("click", () => {
            location.href = `/playlists/${p.playlistId}`;
        });

        grid.appendChild(item);
    });
}


/* ================================
   플레이리스트 생성
================================ */
async function createPlaylist() {

    const name = document.getElementById("playlistNameInput").value.trim();
    const imageFile = document.getElementById("playlistImageInput").files[0];

    if (!name) {
        alert("플레이리스트 이름은 필수입니다.");
        return;
    }

    const formData = new FormData();

    const requestDto = {
        playlistName: name   // 🔥 DTO와 동일
    };

    formData.append(
        "request",
        new Blob([JSON.stringify(requestDto)], { type: "application/json" })
    );

    if (imageFile) {
        formData.append("playlistImage", imageFile);
    }

    try {
        const res = await fetch("/api/playlists", {
            method: "POST",
            headers: { "Authorization": getToken() },
            body: formData
        });

        if (res.status === 401) {
            location.href = "/login";
            return;
        }

        const data = await res.json();

        if (data.success) {

            // 모달 닫기
            document.getElementById("createModal").classList.add("hidden");
            document.getElementById("playlistNameInput").value = "";
            document.getElementById("playlistImageInput").value = "";

            // 다시 조회
            loadPlaylists();

        } else {
            alert(data.message);
        }

    } catch (err) {
        console.error("생성 에러:", err);
    }
}
