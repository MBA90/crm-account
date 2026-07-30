package com.crm.account.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageRequestDTO(Integer page, Integer size, String sortBy, Sort.Direction direction) {

    public Pageable toPageable(String defaultSortBy, Sort.Direction defaultDirection) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String field = sortBy != null && !sortBy.isBlank() ? sortBy : defaultSortBy;
        Sort.Direction sortDirection = direction != null ? direction : defaultDirection;
        return PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, field));
    }
}
