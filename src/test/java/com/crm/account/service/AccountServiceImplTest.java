package com.crm.account.service;

import com.crm.account.domain.Account;
import com.crm.account.domain.enums.AccountSource;
import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.domain.enums.AccountType;
import com.crm.account.domain.enums.EmployeeBand;
import com.crm.account.dto.AccountDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.AccountMapper;
import com.crm.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private UUID accountId;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("defaults the status to Active when the client omits it")
        void defaultsStatusWhenAbsent() {
            AccountDTO request = accountDTO(null, null);
            Account entity = account(null);
            when(accountRepository.existsByRegistrationNoIgnoreCaseAndCountryIgnoreCaseAndErasedAtIsNull(
                    "REG-1", "SA")).thenReturn(false);
            when(accountMapper.toEntity(request)).thenReturn(entity);
            when(accountRepository.save(entity)).thenReturn(entity);
            when(accountMapper.toDTO(entity)).thenReturn(accountDTO(accountId, AccountStatus.ACTIVE));

            AccountDTO created = accountService.create(request);

            assertThat(entity.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(created.status()).isEqualTo(AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("keeps the status the client supplied")
        void keepsSuppliedStatus() {
            AccountDTO request = accountDTO(null, AccountStatus.INACTIVE);
            Account entity = account(null);
            when(accountRepository.existsByRegistrationNoIgnoreCaseAndCountryIgnoreCaseAndErasedAtIsNull(
                    "REG-1", "SA")).thenReturn(false);
            when(accountMapper.toEntity(request)).thenReturn(entity);
            when(accountRepository.save(entity)).thenReturn(entity);
            when(accountMapper.toDTO(entity)).thenReturn(request);

            accountService.create(request);

            assertThat(entity.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        }

        @Test
        @DisplayName("rejects a duplicate registration_no within the same country")
        void rejectsDuplicate() {
            AccountDTO request = accountDTO(null, null);
            when(accountRepository.existsByRegistrationNoIgnoreCaseAndCountryIgnoreCaseAndErasedAtIsNull(
                    "REG-1", "SA")).thenReturn(true);

            assertThatThrownBy(() -> accountService.create(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("REG-1")
                    .hasMessageContaining("SA");

            verify(accountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("copies the updatable fields onto the managed entity")
        void copiesUpdatableFields() {
            Account existing = account(accountId);
            UUID newOwner = UUID.randomUUID();
            UUID parentId = UUID.randomUUID();
            AccountDTO request = new AccountDTO(
                    null, "New Legal", "New Trade", "REG-2", "TAX-2", "Retail",
                    EmployeeBand.BAND_51_200, "EG", "Cairo", "https://new.example",
                    parentId, AccountType.CUSTOMER, newOwner, AccountSource.IMPORT,
                    AccountStatus.INACTIVE, null, null, null, null);

            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.of(existing));
            when(accountRepository.existsByRegistrationNoAndCountryExcludingId("REG-2", "EG", accountId))
                    .thenReturn(false);
            when(accountRepository.save(existing)).thenReturn(existing);
            when(accountMapper.toDTO(existing)).thenReturn(request);

            accountService.update(accountId, request);

            assertThat(existing.getLegalName()).isEqualTo("New Legal");
            assertThat(existing.getTradeName()).isEqualTo("New Trade");
            assertThat(existing.getRegistrationNo()).isEqualTo("REG-2");
            assertThat(existing.getTaxId()).isEqualTo("TAX-2");
            assertThat(existing.getIndustry()).isEqualTo("Retail");
            assertThat(existing.getEmployeeBand()).isEqualTo(EmployeeBand.BAND_51_200);
            assertThat(existing.getCountry()).isEqualTo("EG");
            assertThat(existing.getCity()).isEqualTo("Cairo");
            assertThat(existing.getWebsite()).isEqualTo("https://new.example");
            assertThat(existing.getParentAccountId()).isEqualTo(parentId);
            assertThat(existing.getAccountType()).isEqualTo(AccountType.CUSTOMER);
            assertThat(existing.getOwnerId()).isEqualTo(newOwner);
            assertThat(existing.getSource()).isEqualTo(AccountSource.IMPORT);
            assertThat(existing.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        }

        @Test
        @DisplayName("leaves the current status untouched when the DTO omits it")
        void keepsStatusWhenDtoOmitsIt() {
            Account existing = account(accountId);
            existing.setStatus(AccountStatus.INACTIVE);
            AccountDTO request = accountDTO(accountId, null);

            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.of(existing));
            when(accountRepository.existsByRegistrationNoAndCountryExcludingId("REG-1", "SA", accountId))
                    .thenReturn(false);
            when(accountRepository.save(existing)).thenReturn(existing);
            when(accountMapper.toDTO(existing)).thenReturn(request);

            accountService.update(accountId, request);

            assertThat(existing.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        }

        @Test
        @DisplayName("fails when the account does not exist or was erased")
        void failsWhenMissing() {
            AccountDTO request = accountDTO(accountId, null);
            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.update(accountId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(accountId.toString());
        }

        @Test
        @DisplayName("rejects a registration_no already used by another account")
        void rejectsDuplicate() {
            AccountDTO request = accountDTO(accountId, null);
            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId))
                    .thenReturn(Optional.of(account(accountId)));
            when(accountRepository.existsByRegistrationNoAndCountryExcludingId("REG-1", "SA", accountId))
                    .thenReturn(true);

            assertThatThrownBy(() -> accountService.update(accountId, request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(accountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reads")
    class Reads {

        @Test
        @DisplayName("getById returns the mapped account")
        void getById() {
            Account entity = account(accountId);
            AccountDTO dto = accountDTO(accountId, AccountStatus.ACTIVE);
            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.of(entity));
            when(accountMapper.toDTO(entity)).thenReturn(dto);

            assertThat(accountService.getById(accountId)).isEqualTo(dto);
        }

        @Test
        @DisplayName("getById fails for an erased or unknown account")
        void getByIdMissing() {
            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getById(accountId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("getAll skips erased accounts")
        void getAll() {
            Account entity = account(accountId);
            AccountDTO dto = accountDTO(accountId, AccountStatus.ACTIVE);
            when(accountRepository.findByErasedAtIsNull()).thenReturn(List.of(entity));
            when(accountMapper.toDTO(entity)).thenReturn(dto);

            assertThat(accountService.getAll()).containsExactly(dto);
        }

        @Test
        @DisplayName("getAll returns an empty list when nothing matches")
        void getAllEmpty() {
            when(accountRepository.findByErasedAtIsNull()).thenReturn(List.of());

            assertThat(accountService.getAll()).isEmpty();
        }

        @Test
        @DisplayName("getByOwner filters by owner and status")
        void getByOwner() {
            Account entity = account(accountId);
            AccountDTO dto = accountDTO(accountId, AccountStatus.ACTIVE);
            when(accountRepository.findByOwnerIdAndStatusAndErasedAtIsNull(ownerId, AccountStatus.ACTIVE))
                    .thenReturn(List.of(entity));
            when(accountMapper.toDTO(entity)).thenReturn(dto);

            assertThat(accountService.getByOwner(ownerId, AccountStatus.ACTIVE)).containsExactly(dto);
        }
    }

    @Nested
    @DisplayName("lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("deactivate flips the status and stamps deactivatedAt")
        void deactivate() {
            Account entity = account(accountId);
            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.of(entity));
            when(accountRepository.save(entity)).thenReturn(entity);
            when(accountMapper.toDTO(entity)).thenReturn(accountDTO(accountId, AccountStatus.INACTIVE));

            Instant before = Instant.now();
            accountService.deactivate(accountId);

            assertThat(entity.getStatus()).isEqualTo(AccountStatus.INACTIVE);
            assertThat(entity.getDeactivatedAt()).isNotNull().isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("delete is a soft delete — it stamps erasedAt instead of removing the row")
        void deleteIsSoft() {
            Account entity = account(accountId);
            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.of(entity));

            Instant before = Instant.now();
            accountService.delete(accountId);

            ArgumentCaptor<Account> saved = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(saved.capture());
            assertThat(saved.getValue().getErasedAt()).isNotNull().isAfterOrEqualTo(before);
            verify(accountRepository, never()).delete(any());
        }

        @Test
        @DisplayName("delete fails for an account that was already erased")
        void deleteMissing() {
            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.delete(accountId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("deactivate fails for an unknown account")
        void deactivateMissing() {
            when(accountRepository.findByAccountIdAndErasedAtIsNull(eq(accountId))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.deactivate(accountId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    private AccountDTO accountDTO(UUID id, AccountStatus status) {
        return new AccountDTO(
                id, "Acme LLC", "Acme", "REG-1", "TAX-1", "Software",
                EmployeeBand.BAND_11_50, "SA", "Riyadh", "https://acme.example",
                null, AccountType.PROSPECT, ownerId, AccountSource.API,
                status, null, null, null, null);
    }

    private Account account(UUID id) {
        return Account.builder()
                .accountId(id)
                .legalName("Acme LLC")
                .registrationNo("REG-1")
                .country("SA")
                .accountType(AccountType.PROSPECT)
                .ownerId(ownerId)
                .source(AccountSource.API)
                .status(AccountStatus.ACTIVE)
                .build();
    }
}