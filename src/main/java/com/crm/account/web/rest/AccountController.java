package com.crm.account.web.rest;

import com.crm.account.dto.AccountDTO;
import com.crm.account.dto.AccountSearchCriteriaDTO;
import com.crm.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/search")
    public Page<AccountDTO> search(@RequestBody AccountSearchCriteriaDTO criteria) {
        return accountService.search(criteria);
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
