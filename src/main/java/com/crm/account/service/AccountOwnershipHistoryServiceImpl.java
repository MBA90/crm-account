package com.crm.account.service;

import com.crm.account.domain.AccountOwnershipHistory;
import com.crm.account.dto.AccountOwnershipHistoryDTO;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.AccountOwnershipHistoryMapper;
import com.crm.account.repository.AccountOwnershipHistoryRepository;
import com.crm.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountOwnershipHistoryServiceImpl implements AccountOwnershipHistoryService {

    private final AccountOwnershipHistoryRepository accountOwnershipHistoryRepository;
    private final AccountRepository accountRepository;
    private final AccountOwnershipHistoryMapper accountOwnershipHistoryMapper;

    @Override
    public AccountOwnershipHistoryDTO create(AccountOwnershipHistoryDTO accountOwnershipHistoryDTO) {
        requireAccountExists(accountOwnershipHistoryDTO.accountId());

        AccountOwnershipHistory entry = accountOwnershipHistoryMapper.toEntity(accountOwnershipHistoryDTO);
        return accountOwnershipHistoryMapper.toDTO(accountOwnershipHistoryRepository.save(entry));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountOwnershipHistoryDTO getById(UUID historyId) {
        return accountOwnershipHistoryMapper.toDTO(require(historyId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountOwnershipHistoryDTO> getByAccount(UUID accountId) {
        return accountOwnershipHistoryRepository.findByAccountIdOrderByChangedAtDesc(accountId).stream()
                .map(accountOwnershipHistoryMapper::toDTO)
                .toList();
    }

    private AccountOwnershipHistory require(UUID historyId) {
        return accountOwnershipHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ownership history entry not found: " + historyId));
    }

    private void requireAccountExists(UUID accountId) {
        accountRepository.findByAccountIdAndErasedAtIsNull(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountId));
    }
}
