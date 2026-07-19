package com.crm.account.domain.converter;

import com.crm.account.domain.enums.ConsentSource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link ConsentSource} using its label (e.g. {@code "web_form"}).
 */
@Converter(autoApply = true)
public class ConsentSourceConverter implements AttributeConverter<ConsentSource, String> {

    @Override
    public String convertToDatabaseColumn(ConsentSource attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ConsentSource convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ConsentSource.fromValue(dbData);
    }
}
