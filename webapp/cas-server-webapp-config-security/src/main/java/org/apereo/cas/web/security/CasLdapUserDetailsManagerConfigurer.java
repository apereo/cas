package org.apereo.cas.web.security;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authorization.LdapUserAttributesToRolesAuthorizationGenerator;
import org.apereo.cas.authorization.LdapUserGroupsToRolesAuthorizationGenerator;
import org.apereo.cas.configuration.model.core.web.security.AdminPagesSecurityProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.web.support.WebUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchExecutor;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.ProviderManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link CasLdapUserDetailsManagerConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasLdapUserDetailsManagerConfigurer<B extends ProviderManagerBuilder<B>>
        extends SecurityConfigurerAdapter<AuthenticationManager, B> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasLdapUserDetailsManagerConfigurer.class);

    private final AdminPagesSecurityProperties adminPagesSecurityProperties;

    public CasLdapUserDetailsManagerConfigurer(final AdminPagesSecurityProperties securityProperties) {
        this.adminPagesSecurityProperties = securityProperties;
    }

    private AuthenticationProvider buildLdapAuthenticationProvider() {
        return new LdapAuthenticationProvider(build());
    }

    private AuthorizationGenerator<CommonProfile> build() {
        final LdapAuthorizationProperties ldapAuthz = adminPagesSecurityProperties.getLdap().getLdapAuthz();
        final ConnectionFactory connectionFactory = Beans.newPooledConnectionFactory(ldapAuthz);

        if (StringUtils.isNotBlank(ldapAuthz.getGroupFilter()) && StringUtils.isNotBlank(ldapAuthz.getGroupAttribute())) {
            return new LdapUserGroupsToRolesAuthorizationGenerator(connectionFactory,
                    ldapAuthorizationGeneratorUserSearchExecutor(),
                    ldapAuthz.isAllowMultipleResults(),
                    ldapAuthz.getRoleAttribute(),
                    ldapAuthz.getRolePrefix(),
                    ldapAuthz.getGroupAttribute(),
                    ldapAuthz.getGroupPrefix(),
                    ldapAuthorizationGeneratorGroupSearchExecutor());
        }
        return new LdapUserAttributesToRolesAuthorizationGenerator(connectionFactory,
                ldapAuthorizationGeneratorUserSearchExecutor(),
                ldapAuthz.isAllowMultipleResults(),
                ldapAuthz.getRoleAttribute(),
                ldapAuthz.getRolePrefix());
    }

    private SearchExecutor ldapAuthorizationGeneratorUserSearchExecutor() {
        final LdapAuthorizationProperties ldapAuthz = adminPagesSecurityProperties.getLdap().getLdapAuthz();
        return Beans.newSearchExecutor(ldapAuthz.getBaseDn(), ldapAuthz.getSearchFilter());
    }

    private SearchExecutor ldapAuthorizationGeneratorGroupSearchExecutor() {
        final LdapAuthorizationProperties ldapAuthz = adminPagesSecurityProperties.getLdap().getLdapAuthz();
        return Beans.newSearchExecutor(ldapAuthz.getBaseDn(), ldapAuthz.getGroupFilter());
    }

    @Override
    public void configure(final B builder) throws Exception {
        final AuthenticationProvider provider = postProcess(buildLdapAuthenticationProvider());
        builder.authenticationProvider(provider);
    }

    public class LdapAuthenticationProvider implements AuthenticationProvider {
        private final AuthorizationGenerator<CommonProfile> authorizationGenerator;

        public LdapAuthenticationProvider(final AuthorizationGenerator<CommonProfile> authorizationGenerator) {
            this.authorizationGenerator = authorizationGenerator;
        }

        @Override
        public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
            try {
                final String username = authentication.getPrincipal().toString();
                final Object credentials = authentication.getCredentials();
                final String password = credentials == null ? null : credentials.toString();
                final AuthenticationRequest request = new AuthenticationRequest(username,
                        new org.ldaptive.Credential(password), ReturnAttributes.ALL.value());
                final Authenticator authenticator = Beans.newLdapAuthenticator(adminPagesSecurityProperties.getLdap());
                final AuthenticationResponse response = authenticator.authenticate(request);
                LOGGER.debug("LDAP response: [{}]", response);

                if (response.getResult()) {
                    final LdapEntry entry = response.getLdapEntry();

                    final CommonProfile profile = new CommonProfile();
                    profile.setId(entry.getDn());
                    entry.getAttributes().forEach(a -> profile.addAttribute(a.getName(), a.getStringValues()));

                    this.authorizationGenerator.generate(profile);
                    final Collection<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.addAll(profile.getAttributes().entrySet()
                            .stream().map(e -> new SimpleGrantedAuthority(e.getValue().toString())).collect(Collectors.toList()));
                    authorities.addAll(profile.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                    final RequireAnyRoleAuthorizer authorizer = new RequireAnyRoleAuthorizer(adminPagesSecurityProperties.getAdminRoles());

                    final J2EContext context = new J2EContext(
                            WebUtils.getHttpServletRequestFromRequestAttributes(),
                            WebUtils.getHttpServletResponseFromRequestAttributes());

                    if (authorizer.isAllAuthorized(context, Arrays.asList(profile))) {
                        return new UsernamePasswordAuthenticationToken(username, password, authorities);
                    }
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
}
