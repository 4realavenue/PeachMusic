package com.example.peachmusic.common.web;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.artist.dto.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ArtistPageController {

    private final ArtistService artistService;

    @GetMapping("/artists/{artistId}")
    public String artistDetailPage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long artistId,
            Model model
    ) {
        ArtistGetDetailResponseDto dto = artistService.getArtistDetail(authUser, artistId);

        model.addAttribute("artistId", dto.getArtistId());
        model.addAttribute("artistName", dto.getArtistName());
        model.addAttribute("artistImageUrl", dto.getProfileImage());
        model.addAttribute("country", dto.getCountry());
        model.addAttribute("artistType", dto.getArtistType());
        model.addAttribute("debutDate", dto.getDebutDate());
        model.addAttribute("bio", dto.getBio());
        model.addAttribute("likeCount", dto.getLikeCount());
        model.addAttribute("isLiked", dto.isLiked());

        return "artist/artist-detail";
    }


    /**
     * ✅ 아티스트 앨범 전체 페이지(무한스크롤)
     * - 데이터는 JS가 /api/artists/{artistId}/albums 호출해서 채움
     */
    @GetMapping("/artists/{artistId}/albums/page")
    public String artistAlbumsPage(@PathVariable Long artistId, Model model) {
        model.addAttribute("artistId", artistId);
        return "artist/artist-albums";
    }

    /**
     * ✅ 아티스트 음원 전체 페이지(무한스크롤)
     * - 데이터는 JS가 /api/artists/{artistId}/songs 호출해서 채움
     */
    @GetMapping("/artists/{artistId}/songs/page")
    public String artistSongsPage(@PathVariable Long artistId, Model model) {
        model.addAttribute("artistId", artistId);
        return "artist/artist-songs";
    }
}
