package com.financeapp.utils;

import com.financeapp.models.enums.UserRole;

public class UserRoleConverter extends EnumConverter<UserRole> {
    private UserRoleConverter() {
        super(UserRole.class);
    }

    public static UserRoleConverter createUserRoleConverter() {
        return new UserRoleConverter();
    }
}

