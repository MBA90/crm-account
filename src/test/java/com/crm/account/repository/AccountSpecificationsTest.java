package com.crm.account.repository;

import com.crm.account.domain.Account;
import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.domain.enums.EmployeeBand;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class AccountSpecificationsTest {

    @Mock
    private Root<Account> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Path<String> stringPath;

    @Mock
    private Expression<String> lowered;

    @Mock
    private Predicate predicate;

    @Nested
    @DisplayName("hasStatus")
    class HasStatus {

        @Test
        @DisplayName("is omitted when status is null")
        void nullWhenMissing() {
            assertThat(AccountSpecifications.hasStatus(null).toPredicate(root, query, cb)).isNull();
            verifyNoInteractions(cb);
        }

        @Test
        @DisplayName("filters on the exact status when supplied")
        void equalsWhenSupplied() {
            when(root.get("status")).thenReturn(path);
            when(cb.equal(path, AccountStatus.ACTIVE)).thenReturn(predicate);

            assertThat(AccountSpecifications.hasStatus(AccountStatus.ACTIVE).toPredicate(root, query, cb))
                    .isSameAs(predicate);
        }
    }

    @Nested
    @DisplayName("hasEmployeeBand")
    class HasEmployeeBand {

        @Test
        @DisplayName("is omitted when the band is null")
        void nullWhenMissing() {
            assertThat(AccountSpecifications.hasEmployeeBand(null).toPredicate(root, query, cb)).isNull();
            verifyNoInteractions(cb);
        }

        @Test
        @DisplayName("filters on the exact band when supplied")
        void equalsWhenSupplied() {
            when(root.get("employeeBand")).thenReturn(path);
            when(cb.equal(path, EmployeeBand.BAND_11_50)).thenReturn(predicate);

            assertThat(AccountSpecifications.hasEmployeeBand(EmployeeBand.BAND_11_50).toPredicate(root, query, cb))
                    .isSameAs(predicate);
        }
    }

    @Nested
    @DisplayName("legalNameContains")
    class LegalNameContains {

        @Test
        @DisplayName("is omitted when blank or null")
        void nullWhenBlank() {
            assertThat(AccountSpecifications.legalNameContains(null).toPredicate(root, query, cb)).isNull();
            assertThat(AccountSpecifications.legalNameContains("  ").toPredicate(root, query, cb)).isNull();
            verifyNoInteractions(cb);
        }

        @Test
        @DisplayName("does a case-insensitive contains match")
        void likeWhenSupplied() {
            when(root.<String>get("legalName")).thenReturn(stringPath);
            when(cb.lower(stringPath)).thenReturn(lowered);
            when(cb.like(lowered, "%acme%")).thenReturn(predicate);

            assertThat(AccountSpecifications.legalNameContains("Acme").toPredicate(root, query, cb))
                    .isSameAs(predicate);
        }
    }

    @Nested
    @DisplayName("tradeNameContains")
    class TradeNameContains {

        @Test
        @DisplayName("is omitted when blank or null")
        void nullWhenBlank() {
            assertThat(AccountSpecifications.tradeNameContains(null).toPredicate(root, query, cb)).isNull();
            assertThat(AccountSpecifications.tradeNameContains("").toPredicate(root, query, cb)).isNull();
            verifyNoInteractions(cb);
        }

        @Test
        @DisplayName("does a case-insensitive contains match")
        void likeWhenSupplied() {
            when(root.<String>get("tradeName")).thenReturn(stringPath);
            when(cb.lower(stringPath)).thenReturn(lowered);
            when(cb.like(lowered, "%acme trading%")).thenReturn(predicate);

            assertThat(AccountSpecifications.tradeNameContains("Acme Trading").toPredicate(root, query, cb))
                    .isSameAs(predicate);
        }
    }

    @Nested
    @DisplayName("hasRegistrationNo")
    class HasRegistrationNo {

        @Test
        @DisplayName("is omitted when blank or null")
        void nullWhenBlank() {
            assertThat(AccountSpecifications.hasRegistrationNo(null).toPredicate(root, query, cb)).isNull();
            assertThat(AccountSpecifications.hasRegistrationNo(" ").toPredicate(root, query, cb)).isNull();
            verifyNoInteractions(cb);
        }

        @Test
        @DisplayName("does a case-insensitive exact match")
        void equalsIgnoreCaseWhenSupplied() {
            when(root.<String>get("registrationNo")).thenReturn(stringPath);
            when(cb.lower(stringPath)).thenReturn(lowered);
            when(cb.equal(lowered, "reg-1")).thenReturn(predicate);

            assertThat(AccountSpecifications.hasRegistrationNo("REG-1").toPredicate(root, query, cb))
                    .isSameAs(predicate);
        }
    }
}
