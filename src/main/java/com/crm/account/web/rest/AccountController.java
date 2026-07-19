package com.crm.account.web.rest;

import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.dto.AccountDTO;
import com.crm.account.service.AccountService;
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
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountDTO> list(
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) AccountStatus status) {
        if (ownerId != null) {
            AccountStatus effectiveStatus = status != null ? status : AccountStatus.ACTIVE;
            return accountService.getByOwner(ownerId, effectiveStatus);
        }
        return accountService.getAll();
    }

    @GetMapping("/{accountId}")
    public AccountDTO getById(@PathVariable UUID accountId) {
        return accountService.getById(accountId);
    }

    @PostMapping
    public ResponseEntity<AccountDTO> create(
            @Valid @RequestBody AccountDTO accountDTO,
            UriComponentsBuilder uriBuilder) {
        AccountDTO created = accountService.create(accountDTO);
        URI location = uriBuilder.path("/api/accounts/{accountId}")
                .buildAndExpand(created.accountId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{accountId}")
    public AccountDTO update(
            @PathVariable UUID accountId,
            @Valid @RequestBody AccountDTO accountDTO) {
        return accountService.update(accountId, accountDTO);
    }

    @PostMapping("/{accountId}/deactivate")
    public AccountDTO deactivate(@PathVariable UUID accountId) {
        return accountService.deactivate(accountId);
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID accountId) {
        accountService.delete(accountId);
    }
}
