package com.financeapp.utils;

import jakarta.persistence.AttributeConverter;

public abstract class EnumConverter<E extends Enum<E>> implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    protected EnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return Enum.valueOf(enumClass, dbData);
    }
}