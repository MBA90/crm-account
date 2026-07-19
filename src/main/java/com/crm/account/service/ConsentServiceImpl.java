package com.crm.account.service;

import com.crm.account.domain.Consent;
import com.crm.account.dto.ConsentDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.ConsentMapper;
import com.crm.account.repository.ConsentRepository;
import com.crm.account.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsentServiceImpl implements ConsentService {

    private final ConsentRepository consentRepository;
    private final ContactRepository contactRepository;
    private final ConsentMapper consentMapper;

    @Override
    public ConsentDTO create(ConsentDTO consentDTO) {
        requireContactExists(consentDTO.contactId());

        if (consentRepository.existsByContactIdAndChannelAndPurpose(
                consentDTO.contactId(), consentDTO.channel(), consentDTO.purpose())) {
            throw new DuplicateResourceException(
                    "A consent for channel '%s' and purpose '%s' already exists on contact '%s'"
                            .formatted(consentDTO.channel().getValue(),
                                    consentDTO.purpose().getValue(),
                                    consentDTO.contactId()));
        }

        Consent consent = consentMapper.toEntity(consentDTO);
        return consentMapper.toDTO(consentRepository.save(consent));
    }

    @Override
    @Transactional(readOnly = true)
    public ConsentDTO getById(UUID consentId) {
        return consentMapper.toDTO(require(consentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentDTO> getByContact(UUID contactId) {
        return consentRepository.findByContactId(contactId).stream()
                .map(consentMapper::toDTO)
                .toList();
    }

    @Override
    public ConsentDTO withdraw(UUID consentId) {
        Consent consent = require(consentId);
        consent.setGranted(false);
        consent.setWithdrawnAt(Instant.now());
        return consentMapper.toDTO(consentRepository.save(consent));
    }

    private Consent require(UUID consentId) {
        return consentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consent not found: " + consentId));
    }

    private void requireContactExists(UUID contactId) {
        contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contact not found: " + contactId));
    }
}
