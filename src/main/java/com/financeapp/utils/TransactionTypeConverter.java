package com.financeapp.utils;

import com.financeapp.models.enums.TransactionType;

public class TransactionTypeConverter extends EnumConverter<TransactionType> {
    public TransactionTypeConverter() {
        super(TransactionType.class);
    }
}
