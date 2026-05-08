package com.financeapp.security;


import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class UserPrincipal extends User {

    private final Long userId;
    private final String fullName;

    public UserPrincipal(Long userId, String username, String password,
                         String fullName, boolean enabled,
                         Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.userId = userId;
        this.fullName = fullName;
    }
}