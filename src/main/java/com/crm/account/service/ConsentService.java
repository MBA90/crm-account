package com.crm.account.service;

import com.crm.account.dto.ConsentDTO;

import java.util.List;
import java.util.UUID;

public interface ConsentService {

    /** Grants a consent; at most one per (contact, channel, purpose). */
    ConsentDTO create(ConsentDTO consentDTO);

    ConsentDTO getById(UUID consentId);

    List<ConsentDTO> getByContact(UUID contactId);

    /** Withdraws the consent: stamps {@code withdrawnAt} and sets {@code granted} to false. */
    ConsentDTO withdraw(UUID consentId);
}
