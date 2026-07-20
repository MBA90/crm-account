package com.crm.account.service;

import com.crm.account.domain.Consent;
import com.crm.account.domain.Contact;
import com.crm.account.domain.enums.ConsentChannel;
import com.crm.account.domain.enums.ConsentPurpose;
import com.crm.account.domain.enums.ConsentSource;
import com.crm.account.dto.ConsentDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.ConsentMapper;
import com.crm.account.repository.ConsentRepository;
import com.crm.account.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsentServiceImplTest {

    @Mock
    private ConsentRepository consentRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ConsentMapper consentMapper;

    @InjectMocks
    private ConsentServiceImpl consentService;

    private UUID contactId;
    private UUID consentId;

    @BeforeEach
    void setUp() {
        contactId = UUID.randomUUID();
        consentId = UUID.randomUUID();
    }

    @Test
    @DisplayName("create persists a consent for an existing contact")
    void createPersists() {
        ConsentDTO request = consentDTO(null);
        Consent entity = consent(consentId, true);
        contactExists();
        when(consentRepository.existsByContactIdAndChannelAndPurpose(
                contactId, ConsentChannel.EMAIL, ConsentPurpose.MARKETING)).thenReturn(false);
        when(consentMapper.toEntity(request)).thenReturn(entity);
        when(consentRepository.save(entity)).thenReturn(entity);
        when(consentMapper.toDTO(entity)).thenReturn(consentDTO(consentId));

        assertThat(consentService.create(request).consentId()).isEqualTo(consentId);
    }

    @Test
    @DisplayName("create fails when the contact does not exist")
    void createFailsForUnknownContact() {
        ConsentDTO request = consentDTO(null);
        when(contactRepository.findById(contactId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Contact not found");

        verify(consentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create rejects a second consent for the same channel and purpose")
    void createRejectsDuplicate() {
        ConsentDTO request = consentDTO(null);
        contactExists();
        when(consentRepository.existsByContactIdAndChannelAndPurpose(
                contactId, ConsentChannel.EMAIL, ConsentPurpose.MARKETING)).thenReturn(true);

        assertThatThrownBy(() -> consentService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("marketing");

        verify(consentRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById returns the mapped consent")
    void getById() {
        Consent entity = consent(consentId, true);
        ConsentDTO dto = consentDTO(consentId);
        when(consentRepository.findById(consentId)).thenReturn(Optional.of(entity));
        when(consentMapper.toDTO(entity)).thenReturn(dto);

        assertThat(consentService.getById(consentId)).isEqualTo(dto);
    }

    @Test
    @DisplayName("getById fails for an unknown consent")
    void getByIdMissing() {
        when(consentRepository.findById(consentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consentService.getById(consentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getByContact lists the consents of a contact")
    void getByContact() {
        Consent entity = consent(consentId, true);
        ConsentDTO dto = consentDTO(consentId);
        when(consentRepository.findByContactId(contactId)).thenReturn(List.of(entity));
        when(consentMapper.toDTO(entity)).thenReturn(dto);

        assertThat(consentService.getByContact(contactId)).containsExactly(dto);
    }

    @Test
    @DisplayName("withdraw clears the grant and stamps withdrawnAt")
    void withdraw() {
        Consent entity = consent(consentId, true);
        when(consentRepository.findById(consentId)).thenReturn(Optional.of(entity));
        when(consentRepository.save(entity)).thenReturn(entity);
        when(consentMapper.toDTO(entity)).thenReturn(consentDTO(consentId));

        Instant before = Instant.now();
        consentService.withdraw(consentId);

        assertThat(entity.isGranted()).isFalse();
        assertThat(entity.getWithdrawnAt()).isNotNull().isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("withdraw fails for an unknown consent")
    void withdrawMissing() {
        when(consentRepository.findById(consentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consentService.withdraw(consentId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(consentRepository, never()).save(any());
    }

    private void contactExists() {
        when(contactRepository.findById(contactId))
                .thenReturn(Optional.of(Contact.builder().contactId(contactId).build()));
    }

    private ConsentDTO consentDTO(UUID id) {
        return new ConsentDTO(
                id, contactId, ConsentChannel.EMAIL, ConsentPurpose.MARKETING,
                true, ConsentSource.WEB_FORM, null, null, null);
    }

    private Consent consent(UUID id, boolean granted) {
        return Consent.builder()
                .consentId(id)
                .contactId(contactId)
                .channel(ConsentChannel.EMAIL)
                .purpose(ConsentPurpose.MARKETING)
                .granted(granted)
                .source(ConsentSource.WEB_FORM)
                .build();
    }
}