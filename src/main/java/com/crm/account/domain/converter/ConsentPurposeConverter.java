package com.crm.account.domain.converter;

import com.crm.account.domain.enums.ConsentPurpose;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link ConsentPurpose} using its label (e.g. {@code "marketing"}).
 */
@Converter(autoApply = true)
public class ConsentPurposeConverter implements AttributeConverter<ConsentPurpose, String> {

    @Override
    public String convertToDatabaseColumn(ConsentPurpose attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ConsentPurpose convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ConsentPurpose.fromValue(dbData);
    }
}
