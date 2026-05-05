package com.financeapp.utils;

import com.financeapp.models.enums.AccountType;

public class AccountTypeConverter extends EnumConverter<AccountType> {
    public AccountTypeConverter() {
        super(AccountType.class);
    }
}
