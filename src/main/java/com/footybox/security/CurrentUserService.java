package com.footybox.security;

import com.footybox.common.NotFoundException;
import com.footybox.user.AppUser;
import com.footybox.user.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final AppUserRepository users;

    public CurrentUserService(AppUserRepository users) {
        this.users = users;
    }

    public AppUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new NotFoundException("Current user not found.");
        }
        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Current user not found."));
    }
}
