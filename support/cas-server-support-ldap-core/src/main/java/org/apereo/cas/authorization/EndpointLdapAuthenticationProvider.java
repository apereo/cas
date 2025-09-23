package org.apereo.cas.authorization;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.core.monitor.LdapSecurityActuatorEndpointsMonitorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchOperation;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.Authenticator;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is {@link EndpointLdapAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class EndpointLdapAuthenticationProvider implements AuthenticationProvider {
    private final LdapSecurityActuatorEndpointsMonitorProperties ldapProperties;

    private final SecurityProperties securityProperties;

    private final ConnectionFactory connectionFactory;

    private final Authenticator authenticator;

    private static Authentication generateAuthenticationToken(final Authentication authentication,
                                                              final List<SimpleGrantedAuthority> authorities) {
        val username = authentication.getPrincipal().toString();
        val credentials = authentication.getCredentials();
        return new UsernamePasswordAuthenticationToken(username, credentials, authorities);
    }

    /**
     * Destroy.
     */
    public void destroy() {
        connectionFactory.close();
        authenticator.close();
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        try {
            val username = authentication.getPrincipal().toString();
            val credentials = authentication.getCredentials();
            val password = Optional.ofNullable(credentials).map(Object::toString).orElse(null);
            if (StringUtils.isBlank(password)) {
                throw new IllegalArgumentException("Password cannot be blank");
            }
            LOGGER.debug("Preparing LDAP authentication request for user [{}]", username);
            val request = new AuthenticationRequest(username, new Credential(password), ReturnAttributes.ALL.value());
            LOGGER.debug("Executing LDAP authentication request for user [{}]", username);

            val response = authenticator.authenticate(request);
            LOGGER.debug("LDAP response: [{}]", response);

            if (response.isSuccess()) {
                val requiredRoles = securityProperties
                    .getUser()
                    .getRoles()
                    .stream()
                    .map(role -> Strings.CI.prependIfMissing(role, ldapProperties.getLdapAuthz().getRolePrefix()))
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

                LOGGER.debug("Required roles are [{}]", requiredRoles);
                if (requiredRoles.isEmpty()) {
                    LOGGER.info("No user security roles are defined to enable authorization. User [{}] is considered authorized", username);
                    return generateAuthenticationToken(authentication, new ArrayList<>());
                }

                val entry = response.getLdapEntry();
                val attributes = new HashMap<String, List<Object>>();
                entry.getAttributes().forEach(attribute -> attributes.put(attribute.getName(), new ArrayList<>(attribute.getStringValues())));
                val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(username, attributes);
                val authZGen = buildAuthorizationGenerator();
                var authorities = authZGen.apply(principal);

                LOGGER.debug("List of authorities remapped from profile roles are [{}]", authorities);
                if (authorities.stream().anyMatch(authority -> requiredRoles.contains(authority.getAuthority()))) {
                    return generateAuthenticationToken(authentication, authorities);
                }
                LOGGER.warn("User [{}] is not authorized to access the requested resource", username);
            } else {
                LOGGER.warn("LDAP authentication response produced no results for [{}]", username);
            }

        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            throw new InsufficientAuthenticationException("Unexpected LDAP error", e);
        }
        throw new BadCredentialsException("Could not authenticate provided credentials");
    }

    @Override
    public boolean supports(final Class<?> aClass) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(aClass);
    }

    private Function<Principal, List<SimpleGrantedAuthority>> buildAuthorizationGenerator() {
        val properties = ldapProperties.getLdapAuthz();

        if (isGroupBasedAuthorization()) {
            LOGGER.debug("Handling LDAP authorization based on groups");
            return new LdapUserGroupsToRolesAuthorizationGenerator(
                ldapAuthorizationGeneratorUserSearchOperation(connectionFactory),
                properties.isAllowMultipleResults(),
                properties.getGroupAttribute(),
                properties.getGroupPrefix(),
                ldapAuthorizationGeneratorGroupSearchOperation(connectionFactory));
        }
        if (isUserBasedAuthorization()) {
            LOGGER.debug("Handling LDAP authorization based on attributes and roles");
            return new LdapUserAttributesToRolesAuthorizationGenerator(
                ldapAuthorizationGeneratorUserSearchOperation(connectionFactory),
                properties.isAllowMultipleResults(),
                properties.getRoleAttribute(),
                properties.getRolePrefix());
        }
        val roles = securityProperties.getUser().getRoles();
        LOGGER.info("Authorization will generate static roles based on [{}]", roles);
        return principal -> roles.stream()
            .map(String::toUpperCase)
            .map(role -> Strings.CI.prependIfMissing(role, "ROLE_"))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    private boolean isGroupBasedAuthorization() {
        val properties = ldapProperties.getLdapAuthz();
        return StringUtils.isNotBlank(properties.getGroupFilter()) && StringUtils.isNotBlank(properties.getGroupAttribute());
    }

    private boolean isUserBasedAuthorization() {
        val properties = ldapProperties.getLdapAuthz();
        return StringUtils.isNotBlank(properties.getBaseDn()) && StringUtils.isNotBlank(properties.getSearchFilter());
    }

    private SearchOperation ldapAuthorizationGeneratorUserSearchOperation(final ConnectionFactory factory) {
        val properties = ldapProperties.getLdapAuthz();
        val searchOperation = LdapUtils.newLdaptiveSearchOperation(properties.getBaseDn(), properties.getSearchFilter(),
            new ArrayList<>(), CollectionUtils.wrap(properties.getRoleAttribute()));
        searchOperation.setConnectionFactory(factory);
        return searchOperation;
    }

    private SearchOperation ldapAuthorizationGeneratorGroupSearchOperation(final ConnectionFactory factory) {
        val properties = ldapProperties.getLdapAuthz();
        val searchOperation = LdapUtils.newLdaptiveSearchOperation(properties.getGroupBaseDn(), properties.getGroupFilter(),
            new ArrayList<>(), CollectionUtils.wrap(properties.getGroupAttribute()));
        searchOperation.setConnectionFactory(factory);
        return searchOperation;
    }
}

