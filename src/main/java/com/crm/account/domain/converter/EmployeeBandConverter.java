package com.crm.account.domain.converter;

import com.crm.account.domain.enums.EmployeeBand;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link EmployeeBand} using its label (e.g. {@code "11-50"}).
 */
@Converter(autoApply = true)
public class EmployeeBandConverter implements AttributeConverter<EmployeeBand, String> {

    @Override
    public String convertToDatabaseColumn(EmployeeBand attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public EmployeeBand convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EmployeeBand.fromValue(dbData);
    }
}
