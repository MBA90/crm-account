package com.crm.account.service;

import com.crm.account.domain.Account;
import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.dto.AccountDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.AccountMapper;
import com.crm.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountDTO create(AccountDTO accountDTO) {
        if (accountRepository.existsByRegistrationNoIgnoreCaseAndCountryIgnoreCaseAndErasedAtIsNull(
                accountDTO.registrationNo(), accountDTO.country())) {
            throw new DuplicateResourceException(
                    "An account with registration_no '%s' already exists in country '%s'"
                            .formatted(accountDTO.registrationNo(), accountDTO.country()));
        }

        Account account = accountMapper.toEntity(accountDTO);
        account.setStatus(accountDTO.status() != null ? accountDTO.status() : AccountStatus.ACTIVE);

        return accountMapper.toDTO(accountRepository.save(account));
    }

    @Override
    public AccountDTO update(UUID accountId, AccountDTO accountDTO) {
        Account account = requireActive(accountId);

        if (accountRepository.existsByRegistrationNoAndCountryExcludingId(
                accountDTO.registrationNo(), accountDTO.country(), accountId)) {
            throw new DuplicateResourceException(
                    "An account with registration_no '%s' already exists in country '%s'"
                            .formatted(accountDTO.registrationNo(), accountDTO.country()));
        }

        applyUpdatableFields(accountDTO, account);
        return accountMapper.toDTO(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getById(UUID accountId) {
        return accountMapper.toDTO(requireActive(accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getAll() {
        return accountRepository.findByErasedAtIsNull().stream()
                .map(accountMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getByOwner(UUID ownerId, AccountStatus status) {
        return accountRepository.findByOwnerIdAndStatusAndErasedAtIsNull(ownerId, status).stream()
                .map(accountMapper::toDTO)
                .toList();
    }

    @Override
    public AccountDTO deactivate(UUID accountId) {
        Account account = requireActive(accountId);
        account.setStatus(AccountStatus.INACTIVE);
        account.setDeactivatedAt(Instant.now());
        return accountMapper.toDTO(accountRepository.save(account));
    }

    @Override
    public void delete(UUID accountId) {
        Account account = requireActive(accountId);
        account.setErasedAt(Instant.now());
        accountRepository.save(account);
    }

    private Account requireActive(UUID accountId) {
        return accountRepository.findByAccountIdAndErasedAtIsNull(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountId));
    }

    /** Copies client-updatable fields from the DTO onto a managed entity. */
    private void applyUpdatableFields(AccountDTO dto, Account account) {
        account.setLegalName(dto.legalName());
        account.setTradeName(dto.tradeName());
        account.setRegistrationNo(dto.registrationNo());
        account.setTaxId(dto.taxId());
        account.setIndustry(dto.industry());
        account.setEmployeeBand(dto.employeeBand());
        account.setCountry(dto.country());
        account.setCity(dto.city());
        account.setWebsite(dto.website());
        account.setParentAccountId(dto.parentAccountId());
        account.setAccountType(dto.accountType());
        account.setOwnerId(dto.ownerId());
        account.setSource(dto.source());
        if (dto.status() != null) {
            account.setStatus(dto.status());
        }
    }
}
