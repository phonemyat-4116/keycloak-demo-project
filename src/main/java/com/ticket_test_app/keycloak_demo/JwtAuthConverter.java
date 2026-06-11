package com.ticket_test_app.keycloak_demo;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAttributes;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {

        // Merge all three authority sources into one set
        Collection<GrantedAuthority> authorities = Stream.concat(
                // 1. scope claims → SCOPE_email, SCOPE_profile
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                Stream.concat(
                        // 2. realm_access.roles → ROLE_ADMIN_ROLE, ROLE_OFFLINE_ACCESS ...
                        extractRealmRoles(jwt).stream(),
                        // 3. resource_access.<clientId>.roles → ROLE_CLIENT_ADMIN
                        extractResourceRoles(jwt).stream()
                )
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                getPrincipleClaimName(jwt) // "user1" instead of UUID
        );
    }

    public String getPrincipleClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB; // <- UUID

        if(principleAttributes != null){
            claimName = principleAttributes;
        }
        return jwt.getClaim(claimName); // <- user1
    }

    // Extracts realm-level roles from realm_access.roles
    private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if(realmAccess == null || realmAccess.get("roles") == null) {
            return Set.of();
        }

        Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");

        return realmRoles.stream()
                .map(realmRole -> new SimpleGrantedAuthority("ROLE_" + realmRole))
                .collect(Collectors.toSet());
    }

    // Extracts client-level roles from resource_access.<resourceId>.roles
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource");

        if(resourceAccess == null || resourceAccess.get(resourceId) == null) {
            return Set.of();
        }

        Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(resourceId);

        if(resource == null || resource.get("roles") == null) {
            return Set.of();
        }

        Collection<String> resourceRoles = (Collection<String>) resource.get("roles");

        return resourceRoles.stream()
                .map(resourceRole -> new SimpleGrantedAuthority("ROLE_" + resourceRole))
                .collect(Collectors.toSet());
    }


}

/*
Stream.concat() only accepts exactly 2 streams.

// Stream.concat signature
Stream.concat(Stream<T> a, Stream<T> b)
 */

