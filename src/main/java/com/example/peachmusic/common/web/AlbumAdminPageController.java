package com.example.peachmusic.common.web;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.domain.album.dto.response.ArtistSummaryDto;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistalbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistalbum.repository.ArtistAlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AlbumAdminPageController {

    private final AlbumRepository albumRepository;
    private final ArtistAlbumRepository artistAlbumRepository;

    @GetMapping("/admin/albums")
    public String adminAlbums() {
        return "admin/albums";
    }

    @GetMapping("/admin/albums/create")
    public String albumCreatePage() {
        return "admin/admin-album-create";
    }

    @GetMapping("/admin/albums/{albumId}/update")
    public String adminAlbumUpdatePage(@PathVariable Long albumId, Model model) {

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        List<ArtistAlbum> mappings = artistAlbumRepository.findAllByAlbum_AlbumId(albumId);

        List<ArtistSummaryDto> artistList = mappings.stream()
                .map(aa -> ArtistSummaryDto.from(aa.getArtist()))
                .toList();

        model.addAttribute("album", album);
        model.addAttribute("artistList", artistList);

        return "admin/admin-album-update";
    }

}
