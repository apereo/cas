package org.apereo.cas.web.ldap;

import org.apereo.cas.configuration.model.core.web.security.AdminPagesSecurityProperties;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.ldaptive.LdapEntry;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class LdapAuthenticationProvider implements AuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthenticationProvider.class);

    private final AuthorizationGenerator<CommonProfile> authorizationGenerator;
    private final AdminPagesSecurityProperties adminPagesSecurityProperties;

    public LdapAuthenticationProvider(final AuthorizationGenerator<CommonProfile> authorizationGenerator,
                                      final AdminPagesSecurityProperties adminPagesSecurityProperties) {
        this.authorizationGenerator = authorizationGenerator;
        this.adminPagesSecurityProperties = adminPagesSecurityProperties;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        try {
            final String username = authentication.getPrincipal().toString();
            final Object credentials = authentication.getCredentials();
            final String password = credentials == null ? null : credentials.toString();

            LOGGER.debug("Preparing LDAP authentication request for user [{}]", username);

            final AuthenticationRequest request = new AuthenticationRequest(username, new org.ldaptive.Credential(password), ReturnAttributes.ALL.value());
            final Authenticator authenticator = LdapUtils.newLdaptiveAuthenticator(adminPagesSecurityProperties.getLdap());
            LOGGER.debug("Executing LDAP authentication request for user [{}]", username);

            final AuthenticationResponse response = authenticator.authenticate(request);
            LOGGER.debug("LDAP response: [{}]", response);

            if (response.getResult()) {
                final LdapEntry entry = response.getLdapEntry();

                final CommonProfile profile = new CommonProfile();
                profile.setId(username);
                entry.getAttributes().forEach(a -> profile.addAttribute(a.getName(), a.getStringValues()));

                LOGGER.debug("Collected user profile [{}]", profile);

                this.authorizationGenerator.generate(Pac4jUtils.getPac4jJ2EContext(), profile);
                LOGGER.debug("Assembled user profile with roles after generating authorization claims [{}]", profile);

                final Collection<GrantedAuthority> authorities = new ArrayList<>();
                authorities.addAll(profile.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
                LOGGER.debug("List of authorities remapped from profile roles are [{}]", authorities);

                final RequireAnyRoleAuthorizer authorizer = new RequireAnyRoleAuthorizer(adminPagesSecurityProperties.getAdminRoles());
                LOGGER.debug("Executing authorization for expected admin roles [{}]", authorizer.getElements());

                final J2EContext context = Pac4jUtils.getPac4jJ2EContext();

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
}

