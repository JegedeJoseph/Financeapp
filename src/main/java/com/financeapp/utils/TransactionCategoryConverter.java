package com.financeapp.utils;

import com.financeapp.models.enums.TransactionCategory;

public class TransactionCategoryConverter extends EnumConverter<TransactionCategory> {
    private TransactionCategoryConverter() {
        super(TransactionCategory.class);
    }

    public static TransactionCategoryConverter createTransactionCategoryConverter() {
        return new TransactionCategoryConverter();
    }
}