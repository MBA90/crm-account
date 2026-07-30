package com.crm.account.repository;

import com.crm.account.domain.Account;
import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.domain.enums.EmployeeBand;
import org.springframework.data.jpa.domain.Specification;

/** Dynamic filters for {@link AccountRepository#findAll(Specification, org.springframework.data.domain.Pageable)}. */
public final class AccountSpecifications {

    private AccountSpecifications() {
    }

    public static Specification<Account> hasStatus(AccountStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Account> legalNameContains(String legalName) {
        return (root, query, cb) -> (legalName == null || legalName.isBlank())
                ? null
                : cb.like(cb.lower(root.get("legalName")), "%" + legalName.toLowerCase() + "%");
    }

    public static Specification<Account> tradeNameContains(String tradeName) {
        return (root, query, cb) -> (tradeName == null || tradeName.isBlank())
                ? null
                : cb.like(cb.lower(root.get("tradeName")), "%" + tradeName.toLowerCase() + "%");
    }

    public static Specification<Account> hasRegistrationNo(String registrationNo) {
        return (root, query, cb) -> (registrationNo == null || registrationNo.isBlank())
                ? null
                : cb.equal(cb.lower(root.get("registrationNo")), registrationNo.toLowerCase());
    }

    public static Specification<Account> hasEmployeeBand(EmployeeBand employeeBand) {
        return (root, query, cb) -> employeeBand == null ? null : cb.equal(root.get("employeeBand"), employeeBand);
    }
}
