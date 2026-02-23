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

@Controller
@RequiredArgsConstructor
public class ArtistAdminPageController {

    private final ArtistRepository artistRepository;

    @GetMapping("/admin/artists")
    public String adminArtists() {
        return "admin/artists";
    }

    @GetMapping("/admin/artists/create")
    public String artistCreatePage() {
        return "admin/admin-artist-create";
    }

    @GetMapping("/admin/artists/{artistId}/update")
    public String adminArtistUpdatePage(@PathVariable Long artistId, Model model) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        model.addAttribute("artist", artist);
        return "admin/admin-artist-update";
    }


}

