package com.crm.account.service;

import com.crm.account.domain.Account;
import com.crm.account.domain.Contact;
import com.crm.account.domain.enums.ContactStatus;
import com.crm.account.domain.enums.DecisionRole;
import com.crm.account.dto.ContactDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.ContactMapper;
import com.crm.account.repository.AccountRepository;
import com.crm.account.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ContactMapper contactMapper;

    @InjectMocks
    private ContactServiceImpl contactService;

    private UUID accountId;
    private UUID contactId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        contactId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("defaults the status to Active when the client omits it")
        void defaultsStatus() {
            ContactDTO request = contactDTO(null, "jane@acme.example", false, null);
            Contact entity = contact(contactId, "jane@acme.example", false);
            accountExists();
            when(contactRepository.existsByAccountIdAndEmailIgnoreCase(accountId, "jane@acme.example"))
                    .thenReturn(false);
            when(contactMapper.toEntity(request)).thenReturn(entity);
            when(contactRepository.save(entity)).thenReturn(entity);
            when(contactMapper.toDTO(entity)).thenReturn(request);

            contactService.create(request);

            assertThat(entity.getStatus()).isEqualTo(ContactStatus.ACTIVE);
        }

        @Test
        @DisplayName("demotes the other contacts when the new one is primary")
        void clearsOtherPrimaries() {
            ContactDTO request = contactDTO(null, "jane@acme.example", true, null);
            Contact entity = contact(contactId, "jane@acme.example", true);
            accountExists();
            when(contactRepository.existsByAccountIdAndEmailIgnoreCase(accountId, "jane@acme.example"))
                    .thenReturn(false);
            when(contactMapper.toEntity(request)).thenReturn(entity);
            when(contactRepository.save(entity)).thenReturn(entity);
            when(contactMapper.toDTO(entity)).thenReturn(request);

            contactService.create(request);

            verify(contactRepository).clearPrimaryForOtherContacts(accountId, contactId);
        }

        @Test
        @DisplayName("leaves the existing primary alone for a non-primary contact")
        void keepsOtherPrimariesForNonPrimary() {
            ContactDTO request = contactDTO(null, "jane@acme.example", false, null);
            Contact entity = contact(contactId, "jane@acme.example", false);
            accountExists();
            when(contactRepository.existsByAccountIdAndEmailIgnoreCase(accountId, "jane@acme.example"))
                    .thenReturn(false);
            when(contactMapper.toEntity(request)).thenReturn(entity);
            when(contactRepository.save(entity)).thenReturn(entity);
            when(contactMapper.toDTO(entity)).thenReturn(request);

            contactService.create(request);

            verify(contactRepository, never()).clearPrimaryForOtherContacts(any(), any());
        }

        @Test
        @DisplayName("fails when the owning account does not exist")
        void failsForUnknownAccount() {
            ContactDTO request = contactDTO(null, "jane@acme.example", false, null);
            when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Account not found");

            verify(contactRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejects an email already used on the same account")
        void rejectsDuplicateEmail() {
            ContactDTO request = contactDTO(null, "jane@acme.example", false, null);
            accountExists();
            when(contactRepository.existsByAccountIdAndEmailIgnoreCase(accountId, "jane@acme.example"))
                    .thenReturn(true);

            assertThatThrownBy(() -> contactService.create(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("jane@acme.example");

            verify(contactRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("copies the updatable fields onto the managed entity")
        void copiesUpdatableFields() {
            Contact existing = contact(contactId, "old@acme.example", false);
            ContactDTO request = new ContactDTO(
                    null, accountId, "Janet", "Doe", "janet@acme.example",
                    "+14155552671", "+14155552672", "CTO", "Engineering",
                    false, DecisionRole.DECISION_MAKER, ContactStatus.INACTIVE, null, null);

            when(contactRepository.findById(contactId)).thenReturn(Optional.of(existing));
            accountExists();
            when(contactRepository.existsByAccountIdAndEmailIgnoreCaseAndContactIdNot(
                    accountId, "janet@acme.example", contactId)).thenReturn(false);
            when(contactRepository.save(existing)).thenReturn(existing);
            when(contactMapper.toDTO(existing)).thenReturn(request);

            contactService.update(contactId, request);

            assertThat(existing.getFirstName()).isEqualTo("Janet");
            assertThat(existing.getLastName()).isEqualTo("Doe");
            assertThat(existing.getEmail()).isEqualTo("janet@acme.example");
            assertThat(existing.getPhone()).isEqualTo("+14155552671");
            assertThat(existing.getMobile()).isEqualTo("+14155552672");
            assertThat(existing.getJobTitle()).isEqualTo("CTO");
            assertThat(existing.getDepartment()).isEqualTo("Engineering");
            assertThat(existing.getDecisionRole()).isEqualTo(DecisionRole.DECISION_MAKER);
            assertThat(existing.getStatus()).isEqualTo(ContactStatus.INACTIVE);
        }

        @Test
        @DisplayName("leaves the current status untouched when the DTO omits it")
        void keepsStatusWhenDtoOmitsIt() {
            Contact existing = contact(contactId, "jane@acme.example", false);
            existing.setStatus(ContactStatus.INACTIVE);
            ContactDTO request = contactDTO(contactId, "jane@acme.example", false, null);

            when(contactRepository.findById(contactId)).thenReturn(Optional.of(existing));
            accountExists();
            when(contactRepository.existsByAccountIdAndEmailIgnoreCaseAndContactIdNot(
                    accountId, "jane@acme.example", contactId)).thenReturn(false);
            when(contactRepository.save(existing)).thenReturn(existing);
            when(contactMapper.toDTO(existing)).thenReturn(request);

            contactService.update(contactId, request);

            assertThat(existing.getStatus()).isEqualTo(ContactStatus.INACTIVE);
        }

        @Test
        @DisplayName("promoting a contact to primary demotes the others")
        void promotionClearsOtherPrimaries() {
            Contact existing = contact(contactId, "jane@acme.example", false);
            ContactDTO request = contactDTO(contactId, "jane@acme.example", true, ContactStatus.ACTIVE);

            when(contactRepository.findById(contactId)).thenReturn(Optional.of(existing));
            accountExists();
            when(contactRepository.existsByAccountIdAndEmailIgnoreCaseAndContactIdNot(
                    accountId, "jane@acme.example", contactId)).thenReturn(false);
            when(contactRepository.save(existing)).thenReturn(existing);
            when(contactMapper.toDTO(existing)).thenReturn(request);

            contactService.update(contactId, request);

            assertThat(existing.isPrimary()).isTrue();
            verify(contactRepository).clearPrimaryForOtherContacts(accountId, contactId);
        }

        @Test
        @DisplayName("fails when the contact does not exist")
        void failsForUnknownContact() {
            ContactDTO request = contactDTO(contactId, "jane@acme.example", false, null);
            when(contactRepository.findById(contactId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.update(contactId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Contact not found");
        }

        @Test
        @DisplayName("rejects an email already used by a sibling contact")
        void rejectsDuplicateEmail() {
            ContactDTO request = contactDTO(contactId, "jane@acme.example", false, null);
            when(contactRepository.findById(contactId))
                    .thenReturn(Optional.of(contact(contactId, "old@acme.example", false)));
            accountExists();
            when(contactRepository.existsByAccountIdAndEmailIgnoreCaseAndContactIdNot(
                    accountId, "jane@acme.example", contactId)).thenReturn(true);

            assertThatThrownBy(() -> contactService.update(contactId, request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(contactRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reads")
    class Reads {

        @Test
        @DisplayName("getById returns the mapped contact")
        void getById() {
            Contact entity = contact(contactId, "jane@acme.example", false);
            ContactDTO dto = contactDTO(contactId, "jane@acme.example", false, ContactStatus.ACTIVE);
            when(contactRepository.findById(contactId)).thenReturn(Optional.of(entity));
            when(contactMapper.toDTO(entity)).thenReturn(dto);

            assertThat(contactService.getById(contactId)).isEqualTo(dto);
        }

        @Test
        @DisplayName("getById fails for an unknown contact")
        void getByIdMissing() {
            when(contactRepository.findById(contactId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.getById(contactId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("getByAccount filters by status when one is given")
        void getByAccountWithStatus() {
            Contact entity = contact(contactId, "jane@acme.example", false);
            ContactDTO dto = contactDTO(contactId, "jane@acme.example", false, ContactStatus.ACTIVE);
            when(contactRepository.findByAccountIdAndStatus(accountId, ContactStatus.ACTIVE))
                    .thenReturn(List.of(entity));
            when(contactMapper.toDTO(entity)).thenReturn(dto);

            assertThat(contactService.getByAccount(accountId, ContactStatus.ACTIVE)).containsExactly(dto);
            verify(contactRepository, never()).findByAccountId(any());
        }

        @Test
        @DisplayName("getByAccount returns every status when none is given")
        void getByAccountWithoutStatus() {
            Contact entity = contact(contactId, "jane@acme.example", false);
            ContactDTO dto = contactDTO(contactId, "jane@acme.example", false, ContactStatus.ACTIVE);
            when(contactRepository.findByAccountId(accountId)).thenReturn(List.of(entity));
            when(contactMapper.toDTO(entity)).thenReturn(dto);

            assertThat(contactService.getByAccount(accountId, null)).containsExactly(dto);
            verify(contactRepository, never()).findByAccountIdAndStatus(any(), any());
        }

        @Test
        @DisplayName("getByEmail looks across accounts, case-insensitively")
        void getByEmail() {
            Contact entity = contact(contactId, "jane@acme.example", false);
            ContactDTO dto = contactDTO(contactId, "jane@acme.example", false, ContactStatus.ACTIVE);
            when(contactRepository.findByEmailIgnoreCase("JANE@ACME.EXAMPLE")).thenReturn(List.of(entity));
            when(contactMapper.toDTO(entity)).thenReturn(dto);

            assertThat(contactService.getByEmail("JANE@ACME.EXAMPLE")).containsExactly(dto);
        }

        @Test
        @DisplayName("getByEmail returns an empty list when nobody matches")
        void getByEmailEmpty() {
            when(contactRepository.findByEmailIgnoreCase(anyString())).thenReturn(List.of());

            assertThat(contactService.getByEmail("nobody@acme.example")).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("removes the row — contacts are hard-deleted")
        void deletesRow() {
            Contact entity = contact(contactId, "jane@acme.example", false);
            when(contactRepository.findById(contactId)).thenReturn(Optional.of(entity));

            contactService.delete(contactId);

            verify(contactRepository).delete(entity);
        }

        @Test
        @DisplayName("fails for an unknown contact")
        void failsForUnknownContact() {
            when(contactRepository.findById(contactId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.delete(contactId))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(contactRepository, never()).delete(any(Contact.class));
        }
    }

    private void accountExists() {
        when(accountRepository.findByAccountIdAndErasedAtIsNull(accountId))
                .thenReturn(Optional.of(Account.builder().accountId(accountId).build()));
    }

    private ContactDTO contactDTO(UUID id, String email, boolean primary, ContactStatus status) {
        return new ContactDTO(
                id, accountId, "Jane", "Doe", email,
                "+14155552671", null, "CFO", "Finance",
                primary, DecisionRole.INFLUENCER, status, null, null);
    }

    private Contact contact(UUID id, String email, boolean primary) {
        return Contact.builder()
                .contactId(id)
                .accountId(accountId)
                .firstName("Jane")
                .lastName("Doe")
                .email(email)
                .isPrimary(primary)
                .status(ContactStatus.ACTIVE)
                .build();
    }
}