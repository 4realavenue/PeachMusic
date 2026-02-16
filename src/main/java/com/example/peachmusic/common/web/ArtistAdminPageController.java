package com.example.peachmusic.common.web;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class ArtistAdminPageController {

    private final ArtistRepository artistRepository;

    @GetMapping("/artists")
    public String adminArtists() {
        return "admin/artists";
    }

    @GetMapping("/artists/create")
    public String artistCreatePage() {
        return "admin/admin-artist-create";
    }

    @GetMapping("/artists/{artistId}/update")
    public String adminArtistUpdatePage(@PathVariable Long artistId, Model model) {
        // ✅ API 추가 안 해도 됨: 서버 렌더링으로 현재값만 내려주기
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        model.addAttribute("artist", artist);
        return "admin/admin-artist-update";
    }


}

