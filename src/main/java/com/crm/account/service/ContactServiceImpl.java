package com.crm.account.service;

import com.crm.account.domain.Contact;
import com.crm.account.domain.enums.ContactStatus;
import com.crm.account.dto.ContactDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.ContactMapper;
import com.crm.account.repository.AccountRepository;
import com.crm.account.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final AccountRepository accountRepository;
    private final ContactMapper contactMapper;

    @Override
    public ContactDTO create(ContactDTO contactDTO) {
        requireAccountExists(contactDTO.accountId());

        if (contactRepository.existsByAccountIdAndEmailIgnoreCase(
                contactDTO.accountId(), contactDTO.email())) {
            throw new DuplicateResourceException(
                    "A contact with email '%s' already exists on account '%s'"
                            .formatted(contactDTO.email(), contactDTO.accountId()));
        }

        Contact contact = contactMapper.toEntity(contactDTO);
        contact.setStatus(contactDTO.status() != null ? contactDTO.status() : ContactStatus.ACTIVE);

        Contact saved = contactRepository.save(contact);
        if (saved.isPrimary()) {
            contactRepository.clearPrimaryForOtherContacts(saved.getAccountId(), saved.getContactId());
        }
        return contactMapper.toDTO(saved);
    }

    @Override
    public ContactDTO update(UUID contactId, ContactDTO contactDTO) {
        Contact contact = require(contactId);
        requireAccountExists(contactDTO.accountId());

        if (contactRepository.existsByAccountIdAndEmailIgnoreCaseAndContactIdNot(
                contactDTO.accountId(), contactDTO.email(), contactId)) {
            throw new DuplicateResourceException(
                    "A contact with email '%s' already exists on account '%s'"
                            .formatted(contactDTO.email(), contactDTO.accountId()));
        }

        applyUpdatableFields(contactDTO, contact);
        Contact saved = contactRepository.save(contact);
        if (saved.isPrimary()) {
            contactRepository.clearPrimaryForOtherContacts(saved.getAccountId(), saved.getContactId());
        }
        return contactMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactDTO getById(UUID contactId) {
        return contactMapper.toDTO(require(contactId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactDTO> getByAccount(UUID accountId, ContactStatus status) {
        List<Contact> contacts = (status != null)
                ? contactRepository.findByAccountIdAndStatus(accountId, status)
                : contactRepository.findByAccountId(accountId);
        return contacts.stream().map(contactMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactDTO> getByEmail(String email) {
        return contactRepository.findByEmailIgnoreCase(email).stream()
                .map(contactMapper::toDTO)
                .toList();
    }

    @Override
    public void delete(UUID contactId) {
        Contact contact = require(contactId);
        contactRepository.delete(contact);
    }

    private Contact require(UUID contactId) {
        return contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contact not found: " + contactId));
    }

    private void requireAccountExists(UUID accountId) {
        accountRepository.findByAccountIdAndErasedAtIsNull(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountId));
    }

    /** Copies client-updatable fields from the DTO onto a managed entity. */
    private void applyUpdatableFields(ContactDTO dto, Contact contact) {
        contact.setAccountId(dto.accountId());
        contact.setFirstName(dto.firstName());
        contact.setLastName(dto.lastName());
        contact.setEmail(dto.email());
        contact.setPhone(dto.phone());
        contact.setMobile(dto.mobile());
        contact.setJobTitle(dto.jobTitle());
        contact.setDepartment(dto.department());
        contact.setPrimary(dto.isPrimary());
        contact.setDecisionRole(dto.decisionRole());
        if (dto.status() != null) {
            contact.setStatus(dto.status());
        }
    }
}
