package com.crm.account.domain.converter;

import com.crm.account.domain.enums.AccountType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link AccountType} using its label (e.g. {@code "customer"}).
 */
@Converter(autoApply = true)
public class AccountTypeConverter implements AttributeConverter<AccountType, String> {

    @Override
    public String convertToDatabaseColumn(AccountType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public AccountType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AccountType.fromValue(dbData);
    }
}
