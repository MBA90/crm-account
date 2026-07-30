package com.crm.account.config;

import com.crm.account.config.UserPrincipal;
import com.crm.account.config.UserPrincipalAuthenticationToken;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.UUID;

public class UserPrincipalJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final KeycloakRealmRoleConverter authoritiesConverter = new KeycloakRealmRoleConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);

        UserPrincipal userPrincipal = new UserPrincipal(
            UUID.fromString(jwt.getClaimAsString("userId")),
            jwt.getClaimAsString("username"),
            jwt.getClaimAsString("email"),
            jwt.getClaimAsString("name"),
            jwt.getClaimAsString("department"),
            jwt.getClaimAsString("phone"),
            jwt.getClaimAsString("mobile"));

        return new UserPrincipalAuthenticationToken(userPrincipal,jwt,authorities);
    }
}
