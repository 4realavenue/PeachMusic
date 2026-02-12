import { getToken } from "/js/auth.js";

document.addEventListener("DOMContentLoaded", () => {

    if (!getToken()) {
        location.href = "/login";
        return;
    }

    loadPlaylistDetail();
});

/* ================================
   상세 조회
================================ */
async function loadPlaylistDetail() {

    const playlistId = document.getElementById("playlistId").value;

    const res = await fetch(`/api/playlists/${playlistId}`, {
        headers: { "Authorization": getToken() }
    });

    const data = await res.json();
    if (!data.success) return;

    const playlist = data.data;

    renderHeader(playlist);
    renderSongs(playlist.songList);
}

/* ================================
   상단 렌더링
================================ */
function renderHeader(playlist) {

    document.getElementById("playlistName").textContent =
        playlist.playlistName;

    const imageEl = document.getElementById("playlistImage");

    if (playlist.playlistImage) {
        const imageUrl =
            playlist.playlistImage.startsWith("http")
                ? playlist.playlistImage
                : window.location.origin + playlist.playlistImage;

        imageEl.style.backgroundImage =
            `url("${imageUrl}")`;
    } else {
        imageEl.style.backgroundImage =
            `url("/images/default-playlist.png")`;
    }

    setupNameEdit();
    setupDeletePlaylist();
    setupImageEdit();
}

/* ================================
   이름 수정
================================ */
function setupNameEdit() {

    const area = document.getElementById("nameEditArea");
    const input = document.getElementById("nameInput");

    document.getElementById("nameEditBtn").onclick = () => {
        area.classList.remove("hidden");
        input.value =
            document.getElementById("playlistName").textContent;
    };

    document.getElementById("nameCancelBtn").onclick =
        () => area.classList.add("hidden");

    document.getElementById("nameSaveBtn").onclick = async () => {

        const playlistId =
            document.getElementById("playlistId").value;

        await fetch(`/api/playlists/${playlistId}`, {
            method: "PATCH",
            headers: {
                "Authorization": getToken(),
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                playlistName: input.value.trim()
            })
        });

        area.classList.add("hidden");
        loadPlaylistDetail();
    };
}

/* ================================
   플레이리스트 삭제
================================ */
function setupDeletePlaylist() {

    document.getElementById("deletePlaylistBtn").onclick =
        async () => {

            if (!confirm("플레이리스트를 삭제하시겠습니까?")) return;

            const playlistId =
                document.getElementById("playlistId").value;

            await fetch(`/api/playlists/${playlistId}`, {
                method: "DELETE",
                headers: { "Authorization": getToken() }
            });

            location.href = "/playlists";
        };
}

/* ================================
   이미지 수정
================================ */
function setupImageEdit() {

    const input = document.getElementById("imageInput");

    document.getElementById("imageEditBtn").onclick =
        () => input.click();

    input.onchange = async () => {

        const file = input.files[0];
        if (!file) return;

        const playlistId =
            document.getElementById("playlistId").value;

        const formData = new FormData();
        formData.append("playlistImage", file);

        await fetch(`/api/playlists/${playlistId}/image`, {
            method: "PATCH",
            headers: { "Authorization": getToken() },
            body: formData
        });

        loadPlaylistDetail();
    };
}

/* ================================
   곡 목록 렌더링
================================ */
function renderSongs(songList) {

    const container = document.getElementById("songList");
    container.innerHTML = "";

    const actionBar = document.createElement("div");
    actionBar.className = "song-action-bar";

    actionBar.innerHTML = `
        <label>
            <input type="checkbox" id="selectAllCheckbox">
            전체 선택
        </label>
        <button id="deleteSelectedBtn" class="gray-btn">
            선택 삭제
        </button>
    `;

    container.appendChild(actionBar);

    if (!songList || songList.length === 0) {
        container.innerHTML +=
            `<div style="padding:20px;color:#aaa;">곡이 없습니다.</div>`;
        attachDeleteLogic();
        return;
    }

    songList.forEach(song => {

        const albumImageUrl = song.albumImage
            ? (song.albumImage.startsWith("http")
                ? song.albumImage
                : window.location.origin + song.albumImage)
            : "/images/default-album.png";

        const row = document.createElement("div");
        row.className = "song-row";

        row.innerHTML = `
            <div>
                <input type="checkbox"
                       class="song-check"
                       value="${song.songId}">
            </div>

            <div class="song-cover"
                 style="background-image:url('${albumImageUrl}')">
            </div>

            <div>
                <div class="song-title">${song.name}</div>
                <div class="song-sub">
                    ${song.artistName} - ${song.albumName}
                </div>
            </div>

            <div class="song-like">
                ❤️ ${song.likeCount}
            </div>
        `;

        container.appendChild(row);
    });

    attachDeleteLogic();
}

/* ================================
   곡 삭제
================================ */
function attachDeleteLogic() {

    const selectAll =
        document.getElementById("selectAllCheckbox");

    const deleteBtn =
        document.getElementById("deleteSelectedBtn");

    selectAll.onchange = () => {
        document.querySelectorAll(".song-check")
            .forEach(cb => cb.checked = selectAll.checked);
    };

    deleteBtn.onclick = async () => {

        const checked =
            [...document.querySelectorAll(".song-check:checked")];

        if (checked.length === 0) {
            alert("삭제할 곡을 선택하세요.");
            return;
        }

        const playlistId =
            document.getElementById("playlistId").value;

        const songIdSet =
            checked.map(cb => Number(cb.value));

        await fetch(`/api/playlists/${playlistId}/songs`, {
            method: "DELETE",
            headers: {
                "Authorization": getToken(),
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                songIdSet: songIdSet
            })
        });

        loadPlaylistDetail();
    };
}
