package com.crm.account.web.rest;

import com.crm.account.dto.ConsentDTO;
import com.crm.account.service.ConsentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Consents are legal evidence — no update or delete; changing a preference
 * means withdrawing the existing consent and granting a new one.
 */
@RestController
@RequestMapping("/api/consents")
@RequiredArgsConstructor
public class ConsentController {

    private final ConsentService consentService;

    /** Lists the consents of a contact. */
    @GetMapping
    public List<ConsentDTO> list(@RequestParam UUID contactId) {
        return consentService.getByContact(contactId);
    }

    @GetMapping("/{consentId}")
    public ConsentDTO getById(@PathVariable UUID consentId) {
        return consentService.getById(consentId);
    }

    @PostMapping
    public ResponseEntity<ConsentDTO> create(
            @Valid @RequestBody ConsentDTO consentDTO,
            UriComponentsBuilder uriBuilder) {
        ConsentDTO created = consentService.create(consentDTO);
        URI location = uriBuilder.path("/api/consents/{consentId}")
                .buildAndExpand(created.consentId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/{consentId}/withdraw")
    public ConsentDTO withdraw(@PathVariable UUID consentId) {
        return consentService.withdraw(consentId);
    }
}
