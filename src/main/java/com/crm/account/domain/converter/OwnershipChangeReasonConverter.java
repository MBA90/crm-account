package com.crm.account.domain.converter;

import com.crm.account.domain.enums.OwnershipChangeReason;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link OwnershipChangeReason} using its label (e.g. {@code "reassignment"}).
 */
@Converter(autoApply = true)
public class OwnershipChangeReasonConverter implements AttributeConverter<OwnershipChangeReason, String> {

    @Override
    public String convertToDatabaseColumn(OwnershipChangeReason attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public OwnershipChangeReason convertToEntityAttribute(String dbData) {
        return dbData == null ? null : OwnershipChangeReason.fromValue(dbData);
    }
}
