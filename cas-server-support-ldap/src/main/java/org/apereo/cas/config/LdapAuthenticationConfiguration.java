package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.DefaultAccountStateHandler;
import org.apereo.cas.authentication.support.LdapPasswordPolicyConfiguration;
import org.apereo.cas.authentication.support.OptionalWarningAccountStateHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledCompareAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.control.PasswordPolicyControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Period;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;

/**
 * This is {@link LdapAuthenticationConfiguration} that attempts to create
 * relevant authentication handlers for LDAP.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapAuthenticationConfiguration {

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

    @PostConstruct
    public void initLdapAuthenticationHandlers() {
        casProperties.getAuthn().getLdap().forEach(l -> {
            if (l.getType() != null) {
                final LdapAuthenticationHandler handler = new LdapAuthenticationHandler();
                handler.setServicesManager(servicesManager);
                handler.setAdditionalAttributes(l.getAdditionalAttributes());
                handler.setAllowMultiplePrincipalAttributeValues(l.isAllowMultiplePrincipalAttributeValues());
                handler.setPasswordEncoder(Beans.newPasswordEncoder(l.getPasswordEncoder()));
                handler.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(l.getPrincipalTransformation()));

                final Map<String, String> attributes = new HashMap<>();
                l.getPrincipalAttributeList().forEach(a -> {
                    final String attributeName = a.toString().trim();
                    if (attributeName.contains(":")) {
                        final String[] attrCombo = attributeName.split(":");
                        attributes.put(attrCombo[0].trim(), attrCombo[1].trim());
                    } else {
                        attributes.put(attributeName, attributeName);
                    }
                });
                attributes.putAll(casProperties.getAuthn().getAttributeRepository().getAttributes());
                handler.setPrincipalAttributeMap(attributes);

                handler.setPrincipalIdAttribute(l.getPrincipalAttributeId());

                final Authenticator authenticator = getAuthenticator(l);
                authenticator.setReturnAttributes(attributes.keySet().toString());

                if (l.getPasswordPolicy().isEnabled()) {
                    handler.setPasswordPolicyConfiguration(createLdapPasswordPolicyConfiguration(l, authenticator));
                }

                handler.setAuthenticator(authenticator);
                handler.initialize();

                if (l.getAdditionalAttributes().isEmpty() && l.getPrincipalAttributeList().isEmpty()) {
                    this.authenticationHandlersResolvers.put(handler, this.personDirectoryPrincipalResolver);
                } else {
                    this.authenticationHandlersResolvers.put(handler, null);
                }
            }
        });
    }

    private static LdapPasswordPolicyConfiguration createLdapPasswordPolicyConfiguration(
            final LdapAuthenticationProperties l, final Authenticator authenticator) {

        final LdapPasswordPolicyConfiguration cfg = new LdapPasswordPolicyConfiguration(l.getPasswordPolicy());

        final Set<AuthenticationResponseHandler> handlers = new HashSet<>();
        if (cfg.getPasswordWarningNumberOfDays() > 0) {
            handlers.add(new EDirectoryAuthenticationResponseHandler(
                    Period.ofDays(cfg.getPasswordWarningNumberOfDays())));
            handlers.add(new ActiveDirectoryAuthenticationResponseHandler(
                    Period.ofDays(cfg.getPasswordWarningNumberOfDays())));
            handlers.add(new FreeIPAAuthenticationResponseHandler(
                    Period.ofDays(cfg.getPasswordWarningNumberOfDays()),
                    cfg.getLoginFailures()));
        }
        handlers.add(new PasswordPolicyAuthenticationResponseHandler());
        handlers.add(new PasswordExpirationAuthenticationResponseHandler());
        authenticator.setAuthenticationResponseHandlers((AuthenticationResponseHandler[]) handlers.toArray(
                new AuthenticationResponseHandler[handlers.size()]));

        if (StringUtils.isNotBlank(l.getPasswordPolicy().getWarningAttributeName())
                && StringUtils.isNotBlank(l.getPasswordPolicy().getWarningAttributeValue())) {

            final OptionalWarningAccountStateHandler accountHandler = new OptionalWarningAccountStateHandler();
            accountHandler.setDisplayWarningOnMatch(l.getPasswordPolicy().isDisplayWarningOnMatch());
            accountHandler.setWarnAttributeName(l.getPasswordPolicy().getWarningAttributeName());
            accountHandler.setWarningAttributeValue(l.getPasswordPolicy().getWarningAttributeValue());
            accountHandler.setAttributesToErrorMap(l.getPasswordPolicy().getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
        } else {
            final DefaultAccountStateHandler accountHandler = new DefaultAccountStateHandler();
            accountHandler.setAttributesToErrorMap(l.getPasswordPolicy().getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
        }
        return cfg;
    }

    private static Authenticator getAuthenticator(final LdapAuthenticationProperties l) {
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.AD) {
            return getActiveDirectoryAuthenticator(l);
        }
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.DIRECT) {
            return getDirectBindAuthenticator(l);
        }
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.SASL) {
            return getSaslAuthenticator(l);
        }
        return getAuthenticatedOrAnonSearchAuthenticator(l);
    }

    private static Authenticator getSaslAuthenticator(final LdapAuthenticationProperties l) {
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(Beans.newPooledConnectionFactory(l));
        resolver.setUserFilter(l.getUserFilter());
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
    }

    private static Authenticator getAuthenticatedOrAnonSearchAuthenticator(final LdapAuthenticationProperties l) {
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(Beans.newPooledConnectionFactory(l));
        resolver.setUserFilter(l.getUserFilter());

        final Authenticator auth;
        if (StringUtils.isBlank(l.getPrincipalAttributePassword())) {
            auth = new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
        } else {
            auth = new Authenticator(resolver, getPooledCompareAuthenticationHandler(l));
        }
        auth.setEntryResolver(Beans.newSearchEntryResolver(l));

        return auth;
    }

    private static Authenticator getDirectBindAuthenticator(final LdapAuthenticationProperties l) {
        final FormatDnResolver resolver = new FormatDnResolver(l.getBaseDn());
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
    }

    private static Authenticator getActiveDirectoryAuthenticator(final LdapAuthenticationProperties l) {
        final FormatDnResolver resolver = new FormatDnResolver(l.getDnFormat());
        final Authenticator authn = new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
        authn.setEntryResolver(Beans.newSearchEntryResolver(l));
        return authn;
    }

    private static PooledBindAuthenticationHandler getPooledBindAuthenticationHandler(final LdapAuthenticationProperties l) {
        final PooledBindAuthenticationHandler handler = new PooledBindAuthenticationHandler(Beans.newPooledConnectionFactory(l));
        handler.setAuthenticationControls(new PasswordPolicyControl());
        return handler;
    }

    private static PooledCompareAuthenticationHandler getPooledCompareAuthenticationHandler(final LdapAuthenticationProperties l) {
        final PooledCompareAuthenticationHandler handler = new PooledCompareAuthenticationHandler(
                Beans.newPooledConnectionFactory(l));
        handler.setPasswordAttribute(l.getPrincipalAttributePassword());
        return handler;
    }
}
