package com.crm.account.domain.converter;

import com.crm.account.domain.enums.ContactStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link ContactStatus} using its label (e.g. {@code "Active"}).
 */
@Converter(autoApply = true)
public class ContactStatusConverter implements AttributeConverter<ContactStatus, String> {

    @Override
    public String convertToDatabaseColumn(ContactStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ContactStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ContactStatus.fromValue(dbData);
    }
}
