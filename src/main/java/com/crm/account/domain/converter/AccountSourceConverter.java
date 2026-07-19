package com.crm.account.domain.converter;

import com.crm.account.domain.enums.AccountSource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link AccountSource} using its label (e.g. {@code "admin_panel"}).
 */
@Converter(autoApply = true)
public class AccountSourceConverter implements AttributeConverter<AccountSource, String> {

    @Override
    public String convertToDatabaseColumn(AccountSource attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public AccountSource convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AccountSource.fromValue(dbData);
    }
}
