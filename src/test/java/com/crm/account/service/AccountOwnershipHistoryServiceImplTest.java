package com.crm.account.service;

import com.crm.account.domain.Account;
import com.crm.account.domain.AccountOwnershipHistory;
import com.crm.account.domain.enums.OwnershipChangeReason;
import com.crm.account.dto.AccountOwnershipHistoryDTO;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.AccountOwnershipHistoryMapper;
import com.crm.account.repository.AccountOwnershipHistoryRepository;
import com.crm.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountOwnershipHistoryServiceImplTest {

    @Mock
    private AccountOwnershipHistoryRepository accountOwnershipHistoryRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountOwnershipHistoryMapper accountOwnershipHistoryMapper;

    @InjectMocks
    private AccountOwnershipHistoryServiceImpl service;

    private UUID accountId;
    private UUID historyId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        historyId = UUID.randomUUID();
    }

    @Test
    @DisplayName("create records an entry without touching the account itself")
    void createRecordsEntry() {
        AccountOwnershipHistoryDTO request = historyDTO(null);
        AccountOwnershipHistory entity = history(historyId);
        when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId))
                .thenReturn(Optional.of(Account.builder().accountId(accountId).build()));
        when(accountOwnershipHistoryMapper.toEntity(request)).thenReturn(entity);
        when(accountOwnershipHistoryRepository.save(entity)).thenReturn(entity);
        when(accountOwnershipHistoryMapper.toDTO(entity)).thenReturn(historyDTO(historyId));

        assertThat(service.create(request).historyId()).isEqualTo(historyId);
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("create fails when the account does not exist or was erased")
    void createFailsForUnknownAccount() {
        AccountOwnershipHistoryDTO request = historyDTO(null);
        when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");

        verify(accountOwnershipHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById returns the mapped entry")
    void getById() {
        AccountOwnershipHistory entity = history(historyId);
        AccountOwnershipHistoryDTO dto = historyDTO(historyId);
        when(accountOwnershipHistoryRepository.findById(historyId)).thenReturn(Optional.of(entity));
        when(accountOwnershipHistoryMapper.toDTO(entity)).thenReturn(dto);

        assertThat(service.getById(historyId)).isEqualTo(dto);
    }

    @Test
    @DisplayName("getById fails for an unknown entry")
    void getByIdMissing() {
        when(accountOwnershipHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(historyId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ownership history entry not found");
    }

    @Test
    @DisplayName("getByAccount preserves the newest-first order of the repository")
    void getByAccountKeepsOrder() {
        AccountOwnershipHistory newer = history(historyId);
        UUID olderId = UUID.randomUUID();
        AccountOwnershipHistory older = history(olderId);
        when(accountOwnershipHistoryRepository.findByAccountIdOrderByChangedAtDesc(accountId))
                .thenReturn(List.of(newer, older));
        when(accountOwnershipHistoryMapper.toDTO(newer)).thenReturn(historyDTO(historyId));
        when(accountOwnershipHistoryMapper.toDTO(older)).thenReturn(historyDTO(olderId));

        assertThat(service.getByAccount(accountId))
                .extracting(AccountOwnershipHistoryDTO::historyId)
                .containsExactly(historyId, olderId);
    }

    @Test
    @DisplayName("getByAccount returns an empty trail when there are no changes")
    void getByAccountEmpty() {
        when(accountOwnershipHistoryRepository.findByAccountIdOrderByChangedAtDesc(accountId))
                .thenReturn(List.of());

        assertThat(service.getByAccount(accountId)).isEmpty();
    }

    private AccountOwnershipHistoryDTO historyDTO(UUID id) {
        return new AccountOwnershipHistoryDTO(
                id, accountId, UUID.randomUUID(), UUID.randomUUID(),
                OwnershipChangeReason.REASSIGNMENT, UUID.randomUUID(), null);
    }

    private AccountOwnershipHistory history(UUID id) {
        return AccountOwnershipHistory.builder()
                .historyId(id)
                .accountId(accountId)
                .toOwnerId(UUID.randomUUID())
                .reason(OwnershipChangeReason.REASSIGNMENT)
                .changedBy(UUID.randomUUID())
                .build();
    }
}