package com.footybox.user;

import java.net.URI;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class ProfileMediaController {
    private final ProfileMediaService media;
    public ProfileMediaController(ProfileMediaService media) { this.media = media; }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UserProfileResponse uploadAvatar(@RequestPart("file") MultipartFile file) { return media.save(file, true); }
    @PostMapping(value = "/me/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UserProfileResponse uploadBanner(@RequestPart("file") MultipartFile file) { return media.save(file, false); }
    @DeleteMapping("/me/avatar") UserProfileResponse deleteAvatar() { return media.delete(true); }
    @DeleteMapping("/me/banner") UserProfileResponse deleteBanner() { return media.delete(false); }

    @GetMapping("/{userId}/avatar")
    ResponseEntity<?> avatar(@PathVariable Long userId) { return imageOrFallback(media.read(userId, true), "/assets/avatars/pitch.webp"); }
    @GetMapping("/{userId}/banner")
    ResponseEntity<?> banner(@PathVariable Long userId) { return imageOrFallback(media.read(userId, false), "/assets/editorial/library/stadium-night.webp"); }

    private ResponseEntity<?> imageOrFallback(byte[] bytes, String fallback) {
        if (bytes == null) return ResponseEntity.status(302).location(URI.create(fallback)).build();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_JPEG).body(bytes);
    }
}

