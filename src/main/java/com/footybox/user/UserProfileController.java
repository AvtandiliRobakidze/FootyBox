package com.footybox.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserProfileService profiles;

    public UserProfileController(UserProfileService profiles) {
        this.profiles = profiles;
    }

    @PutMapping("/profile")
    UserProfileResponse update(@Valid @RequestBody UpdateProfileRequest request) {
        return profiles.update(request);
    }
}
