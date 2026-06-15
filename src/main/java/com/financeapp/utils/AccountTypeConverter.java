package com.financeapp.utils;

import com.financeapp.models.enums.AccountType;

public class AccountTypeConverter extends EnumConverter<AccountType> {
    private AccountTypeConverter() {
        super(AccountType.class);
    }

    public static AccountTypeConverter createAccountTypeConverter() {
        return new AccountTypeConverter();
    }
}
