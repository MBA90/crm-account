package com.crm.account.web.rest;

import com.crm.account.dto.AccountOwnershipHistoryDTO;
import com.crm.account.service.AccountOwnershipHistoryService;
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
 * Append-only audit trail of account ownership changes — no update or delete.
 */
@RestController
@RequestMapping("/api/account-ownership-history")
@RequiredArgsConstructor
public class AccountOwnershipHistoryController {

    private final AccountOwnershipHistoryService accountOwnershipHistoryService;

    /** Lists the ownership trail of an account, newest first. */
    @GetMapping
    public List<AccountOwnershipHistoryDTO> list(@RequestParam UUID accountId) {
        return accountOwnershipHistoryService.getByAccount(accountId);
    }

    @GetMapping("/{historyId}")
    public AccountOwnershipHistoryDTO getById(@PathVariable UUID historyId) {
        return accountOwnershipHistoryService.getById(historyId);
    }

    @PostMapping
    public ResponseEntity<AccountOwnershipHistoryDTO> create(
            @Valid @RequestBody AccountOwnershipHistoryDTO accountOwnershipHistoryDTO,
            UriComponentsBuilder uriBuilder) {
        AccountOwnershipHistoryDTO created = accountOwnershipHistoryService.create(accountOwnershipHistoryDTO);
        URI location = uriBuilder.path("/api/account-ownership-history/{historyId}")
                .buildAndExpand(created.historyId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
