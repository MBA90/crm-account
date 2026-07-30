package com.crm.account.dto;

import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.domain.enums.EmployeeBand;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Filters and pagination for {@code POST /api/accounts/search}; null/blank filter fields are ignored. */
public record AccountSearchCriteriaDTO(
        AccountStatus status,
        String legalName,
        String tradeName,
        String registrationNo,
        EmployeeBand employeeBand,
        PageRequestDTO page
) {
    private static final PageRequestDTO DEFAULT_PAGE = new PageRequestDTO(null, null, null, null);

    public Pageable toPageable() {
        return (page != null ? page : DEFAULT_PAGE).toPageable("createdAt", Sort.Direction.DESC);
    }
}
