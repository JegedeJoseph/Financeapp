package com.financeapp.utils;

import com.financeapp.models.enums.InsightType;

public class InsightTypeConverter extends EnumConverter<InsightType> {
    private InsightTypeConverter() {
        super(InsightType.class);
    }

    public static InsightTypeConverter createInsightTypeConverter() {
        return new InsightTypeConverter();
    }
}
