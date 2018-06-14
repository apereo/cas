package org.apereo.cas.web.security.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authorization.LdapUserAttributesToRolesAuthorizationGenerator;
import org.apereo.cas.authorization.LdapUserGroupsToRolesAuthorizationGenerator;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchExecutor;
import org.ldaptive.auth.AuthenticationRequest;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link LdapAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class LdapAuthenticationProvider implements AuthenticationProvider {
    private final MonitorProperties.Endpoints.LdapSecurity ldapProperties;
    private final SecurityProperties securityProperties;

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        try {
            final var username = authentication.getPrincipal().toString();
            final var credentials = authentication.getCredentials();
            final var password = credentials == null ? null : credentials.toString();

            LOGGER.debug("Preparing LDAP authentication request for user [{}]", username);
            final var request = new AuthenticationRequest(username,
                new org.ldaptive.Credential(password), ReturnAttributes.ALL.value());
            final var authenticator = LdapUtils.newLdaptiveAuthenticator(ldapProperties);
            LOGGER.debug("Executing LDAP authentication request for user [{}]", username);

            final var response = authenticator.authenticate(request);
            LOGGER.debug("LDAP response: [{}]", response);

            if (response.getResult()) {
                final var entry = response.getLdapEntry();
                final var profile = new CommonProfile();
                profile.setId(username);
                entry.getAttributes().forEach(a -> profile.addAttribute(a.getName(), a.getStringValues()));

                LOGGER.debug("Collected user profile [{}]", profile);

                final var context = Pac4jUtils.getPac4jJ2EContext();
                final var authZGen = buildAuthorizationGenerator();
                authZGen.generate(context, profile);
                LOGGER.debug("Assembled user profile with roles after generating authorization claims [{}]", profile);

                final Collection<GrantedAuthority> authorities = new ArrayList<>();
                authorities.addAll(profile.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
                LOGGER.debug("List of authorities remapped from profile roles are [{}]", authorities);

                final var authorizer = new RequireAnyRoleAuthorizer(securityProperties.getUser().getRoles());
                LOGGER.debug("Executing authorization for expected admin roles [{}]", authorizer.getElements());

                if (authorizer.isAllAuthorized(context, CollectionUtils.wrap(profile))) {
                    return new UsernamePasswordAuthenticationToken(username, password, authorities);
                }
                LOGGER.warn("User [{}] is not authorized to access the requested resource allowed to roles [{}]",
                    username, authorizer.getElements());
            } else {
                LOGGER.warn("LDAP authentication response produced no results for [{}]", username);
            }

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new InsufficientAuthenticationException("Unexpected LDAP error", e);
        }
        throw new BadCredentialsException("Could not authenticate provided credentials");
    }

    @Override
    public boolean supports(final Class<?> aClass) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(aClass);
    }

    private AuthorizationGenerator<CommonProfile> buildAuthorizationGenerator() {
        final var ldapAuthz = this.ldapProperties.getLdapAuthz();
        final ConnectionFactory connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(this.ldapProperties);

        if (isGroupBasedAuthorization()) {
            LOGGER.debug("Handling LDAP authorization based on groups");
            return new LdapUserGroupsToRolesAuthorizationGenerator(connectionFactory,
                ldapAuthorizationGeneratorUserSearchExecutor(),
                ldapAuthz.isAllowMultipleResults(),
                ldapAuthz.getGroupAttribute(),
                ldapAuthz.getGroupPrefix(),
                ldapAuthorizationGeneratorGroupSearchExecutor());
        }
        LOGGER.debug("Handling LDAP authorization based on attributes and roles");
        return new LdapUserAttributesToRolesAuthorizationGenerator(connectionFactory,
            ldapAuthorizationGeneratorUserSearchExecutor(),
            ldapAuthz.isAllowMultipleResults(),
            ldapAuthz.getRoleAttribute(),
            ldapAuthz.getRolePrefix());
    }

    private boolean isGroupBasedAuthorization() {
        final var ldapAuthz = this.ldapProperties.getLdapAuthz();
        return StringUtils.isNotBlank(ldapAuthz.getGroupFilter()) && StringUtils.isNotBlank(ldapAuthz.getGroupAttribute());
    }

    private SearchExecutor ldapAuthorizationGeneratorUserSearchExecutor() {
        final var ldapAuthz = this.ldapProperties.getLdapAuthz();
        return LdapUtils.newLdaptiveSearchExecutor(ldapAuthz.getBaseDn(), ldapAuthz.getSearchFilter(),
            new ArrayList<>(0), CollectionUtils.wrap(ldapAuthz.getRoleAttribute()));
    }

    private SearchExecutor ldapAuthorizationGeneratorGroupSearchExecutor() {
        final var ldapAuthz = this.ldapProperties.getLdapAuthz();
        return LdapUtils.newLdaptiveSearchExecutor(ldapAuthz.getGroupBaseDn(), ldapAuthz.getGroupFilter(),
            new ArrayList<>(0), CollectionUtils.wrap(ldapAuthz.getGroupAttribute()));
    }
}

