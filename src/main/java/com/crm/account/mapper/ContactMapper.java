package com.crm.account.mapper;

import com.crm.account.domain.Contact;
import com.crm.account.dto.ContactDTO;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    @Mapping(target = "contactId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Contact toEntity(ContactDTO contactDTO);

    @Mapping(target = "isPrimary", source = "primary")
    ContactDTO toDTO(Contact contact);
}
