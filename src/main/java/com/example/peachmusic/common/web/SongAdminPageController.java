package com.example.peachmusic.common.web;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class SongAdminPageController {

    private final SongRepository songRepository;

    @GetMapping("/admin/songs")
    public String adminSongs() {
        return "admin/songs"; // 목록 페이지(원하면 나중에 연결)
    }

    @GetMapping("/admin/songs/create")
    public String adminSongCreatePage() {
        return "admin/admin-song-create";
    }

    @GetMapping("/admin/songs/{songId}/update")
    public String songUpdatePage(@PathVariable Long songId, Model model) {

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        model.addAttribute("song", song);

        return "admin/admin-song-update";
    }
}

