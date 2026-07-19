package com.crm.account.domain.converter;

import com.crm.account.domain.enums.DecisionRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link DecisionRole} using its label (e.g. {@code "decision_maker"}).
 */
@Converter(autoApply = true)
public class DecisionRoleConverter implements AttributeConverter<DecisionRole, String> {

    @Override
    public String convertToDatabaseColumn(DecisionRole attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public DecisionRole convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DecisionRole.fromValue(dbData);
    }
}
