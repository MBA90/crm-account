package com.crm.account.domain.converter;

import com.crm.account.domain.enums.ConsentChannel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link ConsentChannel} using its label (e.g. {@code "email"}).
 */
@Converter(autoApply = true)
public class ConsentChannelConverter implements AttributeConverter<ConsentChannel, String> {

    @Override
    public String convertToDatabaseColumn(ConsentChannel attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ConsentChannel convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ConsentChannel.fromValue(dbData);
    }
}
