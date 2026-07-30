package com.crm.account.repository;

import com.crm.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {

    /** Fetch a non-erased account by id. */
    Optional<Account> findByAccountIdAndErasedAtIsNull(UUID accountId);

    /** Direct children of an account. */
    List<Account> findByParentAccountIdAndErasedAtIsNull(UUID parentAccountId);

    /** Enforces the (registration_no, country) uniqueness at the application layer. */
    boolean existsByRegistrationNoIgnoreCaseAndCountryIgnoreCaseAndErasedAtIsNull(String registrationNo, String country);

    /** Same check excluding a given account (used on update). */
    @Query("""
            select case when count(a) > 0 then true else false end
            from Account a
            where lower(a.registrationNo) = lower(:registrationNo)
              and lower(a.country) = lower(:country)
              and a.erasedAt is null
              and a.accountId <> :excludeId
            """)
    boolean existsByRegistrationNoAndCountryExcludingId(
            @Param("registrationNo") String registrationNo,
            @Param("country") String country,
            @Param("excludeId") UUID excludeId);
}
