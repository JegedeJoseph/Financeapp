package com.financeapp.utils;

import com.financeapp.models.enums.TransactionCategory;

public class TransactionCategoryConverter extends EnumConverter<TransactionCategory> {
    public TransactionCategoryConverter() {
        super(TransactionCategory.class);
    }
}