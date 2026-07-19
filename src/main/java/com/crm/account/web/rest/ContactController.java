package com.crm.account.web.rest;

import com.crm.account.domain.enums.ContactStatus;
import com.crm.account.dto.ContactDTO;
import com.crm.account.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * Lists contacts. Filter by {@code accountId} (optionally with {@code status}),
     * or perform a cross-account lookup by {@code email}.
     */
    @GetMapping
    public List<ContactDTO> list(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) ContactStatus status,
            @RequestParam(required = false) String email) {
        if (email != null) {
            return contactService.getByEmail(email);
        }
        return contactService.getByAccount(accountId, status);
    }

    @GetMapping("/{contactId}")
    public ContactDTO getById(@PathVariable UUID contactId) {
        return contactService.getById(contactId);
    }

    @PostMapping
    public ResponseEntity<ContactDTO> create(
            @Valid @RequestBody ContactDTO contactDTO,
            UriComponentsBuilder uriBuilder) {
        ContactDTO created = contactService.create(contactDTO);
        URI location = uriBuilder.path("/api/contacts/{contactId}")
                .buildAndExpand(created.contactId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{contactId}")
    public ContactDTO update(
            @PathVariable UUID contactId,
            @Valid @RequestBody ContactDTO contactDTO) {
        return contactService.update(contactId, contactDTO);
    }

    @DeleteMapping("/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID contactId) {
        contactService.delete(contactId);
    }
}
