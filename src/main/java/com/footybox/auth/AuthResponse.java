package com.footybox.auth;

import com.footybox.user.UserProfileResponse;

public record AuthResponse(String token, UserProfileResponse user) {
}
