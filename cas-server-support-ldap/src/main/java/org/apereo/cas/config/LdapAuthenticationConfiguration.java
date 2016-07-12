package org.apereo.cas.config;

import org.apache.shiro.ldap.UnsupportedAuthenticationMechanismException;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.authorization.generator.LdapAuthorizationGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchExecutor;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.auth.SearchEntryResolver;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.control.PasswordPolicyControl;
import org.pac4j.core.authorization.AuthorizationGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link LdapAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapAuthenticationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthenticationConfiguration.class);

    @Autowired(required = false)
    @Qualifier("ldapAuthorizationGeneratorConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Autowired(required = false)
    @Qualifier("ldapAuthorizationGeneratorUserSearchExecutor")
    private SearchExecutor userSearchExecutor;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("ldapPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration ldapPasswordPolicyConfiguration;

    @Bean
    @RefreshScope
    public AuthorizationGenerator ldapAuthorizationGenerator() {
        if (connectionFactory != null) {
            final LdapAuthorizationGenerator gen =
                    new LdapAuthorizationGenerator(this.connectionFactory, this.userSearchExecutor);
            gen.setAllowMultipleResults(casProperties.getLdapAuthz().isAllowMultipleResults());
            gen.setRoleAttribute(casProperties.getLdapAuthz().getRoleAttribute());
            gen.setRolePrefix(casProperties.getLdapAuthz().getRolePrefix());
            return gen;
        }

        return commonProfile -> {
            throw new UnsupportedAuthenticationMechanismException("Authorization generator not specified");
        };
    }

    @PostConstruct
    public void initLdapAuthenticationHandlers() {
        casProperties.getAuthn().getLdap().forEach(l -> {
            if (l.getType() != null) {
                final LdapAuthenticationHandler handler = new LdapAuthenticationHandler();
                handler.setServicesManager(servicesManager);
                handler.setAdditionalAttributes(l.getAdditionalAttributes());
                handler.setAllowMultiplePrincipalAttributeValues(l.isAllowMultiplePrincipalAttributeValues());

                final Map<String, String> attributes = new HashMap<>();
                l.getPrincipalAttributeList().forEach(a -> attributes.put(a.toString(), a.toString()));
                attributes.putAll(casProperties.getAuthn().getAttributeRepository().getAttributes());
                handler.setPrincipalAttributeMap(attributes);

                handler.setPrincipalIdAttribute(l.getPrincipalAttributeId());

                final Authenticator authenticator = getAuthenticator(l);
                if (l.isUsePasswordPolicy()) {
                    authenticator.setAuthenticationResponseHandlers(
                            new ActiveDirectoryAuthenticationResponseHandler(
                                    TimeUnit.DAYS.convert(this.ldapPasswordPolicyConfiguration.getPasswordWarningNumberOfDays(),
                                            TimeUnit.MILLISECONDS)
                            ),
                            new PasswordPolicyAuthenticationResponseHandler(),
                            new PasswordExpirationAuthenticationResponseHandler());

                    handler.setPasswordPolicyConfiguration(this.ldapPasswordPolicyConfiguration);
                }
                handler.setAuthenticator(authenticator);

                if (l.getAdditionalAttributes().isEmpty() && l.getPrincipalAttributeList().isEmpty()) {
                    this.authenticationHandlersResolvers.put(handler, this.personDirectoryPrincipalResolver);
                } else {
                    this.authenticationHandlersResolvers.put(handler, null);
                }
            }
        });
    }

    private static Authenticator getAuthenticator(final LdapAuthenticationProperties l) {
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.AD) {
            return getActiveDirectoryAuthenticator(l);
        }
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.DIRECT) {
            return getDirectBindAuthenticator(l);
        }
        return getAuthenticatedOrAnonSearchAuthenticator(l);
    }

    private static Authenticator getAuthenticatedOrAnonSearchAuthenticator(final LdapAuthenticationProperties l) {
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(Beans.newPooledConnectionFactory(l));
        resolver.setUserFilter(l.getUserFilter());
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
    }

    private static Authenticator getDirectBindAuthenticator(final LdapAuthenticationProperties l) {
        final FormatDnResolver resolver = new FormatDnResolver(l.getBaseDn());
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
    }

    private static Authenticator getActiveDirectoryAuthenticator(final LdapAuthenticationProperties l) {
        final FormatDnResolver resolver = new FormatDnResolver(l.getDnFormat());
        final Authenticator authn = new Authenticator(resolver, getPooledBindAuthenticationHandler(l));

        final SearchEntryResolver entryResolver = new SearchEntryResolver();
        entryResolver.setBaseDn(l.getBaseDn());
        entryResolver.setUserFilter(l.getUserFilter());
        entryResolver.setSubtreeSearch(l.isSubtreeSearch());
        authn.setEntryResolver(new SearchEntryResolver());

        return authn;
    }

    private static PooledBindAuthenticationHandler getPooledBindAuthenticationHandler(final LdapAuthenticationProperties l) {
        final PooledBindAuthenticationHandler handler = new PooledBindAuthenticationHandler(Beans.newPooledConnectionFactory(l));
        handler.setAuthenticationControls(new PasswordPolicyControl());
        return handler;
    }
}

