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

    document.getElementById("createPlaylistBtn")
        .addEventListener("click", () => {
            const name = prompt("플레이리스트 이름을 입력하세요");
            if (!name) return;
            createPlaylist(name);
        });
});

async function loadPlaylists() {

    const res = await fetch("/api/playlists", {
        headers: { "Authorization": getToken() }
    });

    if (res.status === 401) {
        location.href = "/login";
        return;
    }

    const data = await res.json();
    if (!data.success) return;

    renderPlaylists(data.data);
}

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
                 style="background-image:url('${p.imageUrl || ""}');
                        background-size:cover;
                        background-position:center;">
            </div>
            <div class="playlist-label">${p.name}</div>
        `;

        grid.appendChild(item);
    });
}
