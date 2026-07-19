package com.crm.account.service;

import com.crm.account.domain.enums.ContactStatus;
import com.crm.account.dto.ContactDTO;

import java.util.List;
import java.util.UUID;

public interface ContactService {

    ContactDTO create(ContactDTO contactDTO);

    ContactDTO update(UUID contactId, ContactDTO contactDTO);

    ContactDTO getById(UUID contactId);

    List<ContactDTO> getByAccount(UUID accountId, ContactStatus status);

    /** Cross-account lookup by email (case-insensitive). */
    List<ContactDTO> getByEmail(String email);

    void delete(UUID contactId);
}
