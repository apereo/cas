package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.EchoingPrincipalResolver;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.authentication.principal.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.DefaultAccountStateHandler;
import org.apereo.cas.authentication.support.LdapPasswordPolicyConfiguration;
import org.apereo.cas.authentication.support.OptionalWarningAccountStateHandler;
import org.apereo.cas.config.support.authentication.AuthenticationEventExecutionPlanConfigurer;
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
import org.ldaptive.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Period;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapAuthenticationConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;


    @ConditionalOnMissingBean(name = "ldapPrincipalFactory")
    @Bean
    public PrincipalFactory ldapPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public Collection<AuthenticationHandler> ldapAuthenticationHandlers() {
        final Collection<AuthenticationHandler> handlers = new HashSet<>();

        casProperties.getAuthn().getLdap()
                .stream()
                .filter(ldapInstanceConfigurationPredicate())
                .forEach(l -> {
                    final Map<String, String> attributes = buildPrincipalAttributeMap(l);
                    LOGGER.debug("Creating ldap authenticator for {} and baseDn {}", l.getLdapUrl(), l.getBaseDn());
                    final Authenticator authenticator = getAuthenticator(l);
                    authenticator.setReturnAttributes(attributes.keySet().toArray(new String[]{}));
                    LOGGER.debug("Ldap authenticator configured with return attributes {} for {} and baseDn {}",
                            attributes.keySet(), l.getLdapUrl(), l.getBaseDn());

                    LOGGER.debug("Creating ldap authentication handler for {}", l.getLdapUrl());
                    final LdapAuthenticationHandler handler = new LdapAuthenticationHandler(authenticator);
                    handler.setServicesManager(servicesManager);
                    handler.setName(l.getName());
                    handler.setOrder(l.getOrder());
                    handler.setPrincipalFactory(ldapPrincipalFactory());

                    final List<String> additionalAttrs = l.getAdditionalAttributes();
                    if (StringUtils.isNotBlank(l.getPrincipalAttributeId())) {
                        additionalAttrs.add(l.getPrincipalAttributeId());
                    }
                    handler.setAdditionalAttributes(additionalAttrs);
                    handler.setAllowMultiplePrincipalAttributeValues(l.isAllowMultiplePrincipalAttributeValues());
                    handler.setPasswordEncoder(Beans.newPasswordEncoder(l.getPasswordEncoder()));
                    handler.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(l.getPrincipalTransformation()));

                    if (StringUtils.isNotBlank(l.getCredentialCriteria())) {
                        LOGGER.debug("Ldap authentication for {} is filtering credentials by {}", l.getLdapUrl(), l.getCredentialCriteria());
                        final Predicate<String> predicate = Pattern.compile(l.getCredentialCriteria()).asPredicate();
                        handler.setCredentialSelectionPredicate(credential -> predicate.test(credential.getId()));
                    }

                    handler.setPrincipalAttributeMap(attributes);
                    if (StringUtils.isBlank(l.getPrincipalAttributeId())) {
                        LOGGER.debug("No principal id attribute is found for ldap authentication via {}", l.getLdapUrl());
                    } else {
                        handler.setPrincipalIdAttribute(l.getPrincipalAttributeId());
                        LOGGER.debug("Using principal id attribute {} for ldap authentication via {}", l.getPrincipalAttributeId(),
                                l.getLdapUrl());
                    }

                    if (l.getPasswordPolicy().isEnabled()) {
                        LOGGER.debug("Password policy is enabled for {}. Constructing password policy configuration", l.getLdapUrl());
                        handler.setPasswordPolicyConfiguration(createLdapPasswordPolicyConfiguration(l, authenticator));
                    }

                    LOGGER.debug("Initializing ldap authentication handler for {}", l.getLdapUrl());
                    handler.initialize();
                    handlers.add(handler);
                });
        return handlers;
    }

    private Map<String, String> buildPrincipalAttributeMap(final LdapAuthenticationProperties l) {
        final Map<String, String> attributes = new HashMap<>();

        if (l.getPrincipalAttributeList().isEmpty()) {
            LOGGER.debug("No principal attributes are defined for {}", l.getLdapUrl());
        } else {
            l.getPrincipalAttributeList().forEach(a -> {
                final String attributeName = a.toString().trim();
                if (attributeName.contains(":")) {
                    final String[] attrCombo = attributeName.split(":");
                    final String name = attrCombo[0].trim();
                    final String value = attrCombo[1].trim();
                    LOGGER.debug("Mapped principal attribute name {} to {} for {}", name, value, l.getLdapUrl());
                    attributes.put(name, value);
                } else {
                    LOGGER.debug("Mapped principal attribute name {} for {}", attributeName, l.getLdapUrl());
                    attributes.put(attributeName, attributeName);
                }
            });
        }
        attributes.putAll(casProperties.getAuthn().getAttributeRepository().getAttributes());

        LOGGER.debug("Ldap authentication for {} is configured with principal attributes {}...", l.getLdapUrl(), attributes);
        return attributes;
    }

    private Predicate<LdapAuthenticationProperties> ldapInstanceConfigurationPredicate() {
        return l -> {
            if (l.getType() == null) {
                LOGGER.warn("Skipping ldap authentication entry since no type is defined");
                return false;
            }
            if (StringUtils.isBlank(l.getBaseDn())) {
                LOGGER.warn("Skipping ldap authentication entry since no baseDn is defined");
                return false;
            }
            if (StringUtils.isBlank(l.getLdapUrl())) {
                LOGGER.warn("Skipping ldap authentication entry since no ldap url is defined");
                return false;
            }
            if (StringUtils.isBlank(l.getUserFilter())) {
                LOGGER.warn("Skipping ldap authentication entry since no user filter is defined");
                return false;
            }
            return true;
        };
    }

    private static LdapPasswordPolicyConfiguration createLdapPasswordPolicyConfiguration(final LdapAuthenticationProperties l,
                                                                                         final Authenticator authenticator) {
        final LdapPasswordPolicyConfiguration cfg = new LdapPasswordPolicyConfiguration(l.getPasswordPolicy());
        final Set<AuthenticationResponseHandler> handlers = new HashSet<>();
        if (cfg.getPasswordWarningNumberOfDays() > 0) {
            LOGGER.debug("Password policy authentication response handler is set to accommodate directory type: {}", l.getPasswordPolicy().getType());
            switch (l.getPasswordPolicy().getType()) {
                case AD:
                    handlers.add(new ActiveDirectoryAuthenticationResponseHandler(Period.ofDays(cfg.getPasswordWarningNumberOfDays())));
                    break;
                case FreeIPA:
                    handlers.add(new FreeIPAAuthenticationResponseHandler(Period.ofDays(cfg.getPasswordWarningNumberOfDays()), cfg.getLoginFailures()));
                    break;
                case EDirectory:
                    handlers.add(new EDirectoryAuthenticationResponseHandler(Period.ofDays(cfg.getPasswordWarningNumberOfDays())));
                    break;
                default:
                    handlers.add(new PasswordPolicyAuthenticationResponseHandler());
                    handlers.add(new PasswordExpirationAuthenticationResponseHandler());
                    break;
            }
        } else {
            LOGGER.debug("Password warning number of days is undefined; LDAP authentication may NOT support "
                    + "EDirectory, AD and FreeIPA to handle password policy authentication responses");
        }
        authenticator.setAuthenticationResponseHandlers((AuthenticationResponseHandler[]) handlers.toArray(
                new AuthenticationResponseHandler[handlers.size()]));

        LOGGER.debug("LDAP authentication response handlers configured are: {}", handlers);

        if (StringUtils.isNotBlank(l.getPasswordPolicy().getWarningAttributeName())
                && StringUtils.isNotBlank(l.getPasswordPolicy().getWarningAttributeValue())) {

            LOGGER.debug("Configuring an warning account state handler for LDAP authentication for warning attribute {} and value {}",
                    l.getPasswordPolicy().getWarningAttributeName(), l.getPasswordPolicy().getWarningAttributeValue());

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
            LOGGER.debug("Configuring the default account state handler for LDAP authentication");
        }
        return cfg;
    }

    private static Authenticator getAuthenticator(final LdapAuthenticationProperties l) {
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.AD) {
            LOGGER.debug("Creating active directory authenticator for {}", l.getLdapUrl());
            return getActiveDirectoryAuthenticator(l);
        }
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.DIRECT) {
            LOGGER.debug("Creating direct-bind authenticator for {}", l.getLdapUrl());
            return getDirectBindAuthenticator(l);
        }
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.SASL) {
            LOGGER.debug("Creating SASL authenticator for {}", l.getLdapUrl());
            return getSaslAuthenticator(l);
        }
        if (l.getType() == LdapAuthenticationProperties.AuthenticationTypes.AUTHENTICATED) {
            LOGGER.debug("Creating authenticated authenticator for {}", l.getLdapUrl());
            return getAuthenticatedOrAnonSearchAuthenticator(l);
        }

        LOGGER.debug("Creating anonymous authenticator for {}", l.getLdapUrl());
        return getAuthenticatedOrAnonSearchAuthenticator(l);
    }

    private static Authenticator getSaslAuthenticator(final LdapAuthenticationProperties l) {
        final PooledConnectionFactory factory = Beans.newPooledConnectionFactory(l);
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(Beans.newPooledConnectionFactory(l));
        resolver.setUserFilter(l.getUserFilter());
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(l, factory));
    }

    private static Authenticator getAuthenticatedOrAnonSearchAuthenticator(final LdapAuthenticationProperties l) {
        final PooledConnectionFactory factory = Beans.newPooledConnectionFactory(l);
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(Beans.newPooledConnectionFactory(l));
        resolver.setUserFilter(l.getUserFilter());

        final Authenticator auth;
        if (StringUtils.isBlank(l.getPrincipalAttributePassword())) {
            auth = new Authenticator(resolver, getPooledBindAuthenticationHandler(l, factory));
        } else {
            auth = new Authenticator(resolver, getPooledCompareAuthenticationHandler(l, factory));
        }

        if (l.isEnhanceWithEntryResolver()) {
            auth.setEntryResolver(Beans.newSearchEntryResolver(l, factory));
        }
        return auth;
    }

    private static Authenticator getDirectBindAuthenticator(final LdapAuthenticationProperties l) {
        final PooledConnectionFactory factory = Beans.newPooledConnectionFactory(l);
        final FormatDnResolver resolver = new FormatDnResolver(l.getBaseDn());
        final Authenticator authenticator = new Authenticator(resolver, getPooledBindAuthenticationHandler(l, factory));

        if (l.isEnhanceWithEntryResolver()) {
            authenticator.setEntryResolver(Beans.newSearchEntryResolver(l, factory));
        }
        return authenticator;
    }

    private static Authenticator getActiveDirectoryAuthenticator(final LdapAuthenticationProperties l) {
        if (StringUtils.isBlank(l.getDnFormat())) {
            throw new IllegalArgumentException("Dn format cannot be empty/blank for active directory authentication");
        }
        final PooledConnectionFactory factory = Beans.newPooledConnectionFactory(l);
        final FormatDnResolver resolver = new FormatDnResolver(l.getDnFormat());
        final Authenticator authn = new Authenticator(resolver, getPooledBindAuthenticationHandler(l, factory));

        if (l.isEnhanceWithEntryResolver()) {
            authn.setEntryResolver(Beans.newSearchEntryResolver(l, factory));
        }
        return authn;
    }

    private static PooledBindAuthenticationHandler getPooledBindAuthenticationHandler(final LdapAuthenticationProperties l,
                                                                                      final PooledConnectionFactory factory) {
        final PooledBindAuthenticationHandler handler = new PooledBindAuthenticationHandler(factory);
        handler.setAuthenticationControls(new PasswordPolicyControl());
        return handler;
    }

    private static PooledCompareAuthenticationHandler getPooledCompareAuthenticationHandler(final LdapAuthenticationProperties l,
                                                                                            final PooledConnectionFactory factory) {
        final PooledCompareAuthenticationHandler handler = new PooledCompareAuthenticationHandler(factory);
        handler.setPasswordAttribute(l.getPrincipalAttributePassword());
        return handler;
    }

    /**
     * The type Ldap authentication event execution plan configuration.
     */
    @Configuration("ldapAuthenticationEventExecutionPlanConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class LdapAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            ldapAuthenticationHandlers().forEach(handler -> {
                final ChainingPrincipalResolver resolver = new ChainingPrincipalResolver();
                LOGGER.debug("Ldap authentication for {} is to chain principal resolvers for attribute resolution", handler.getName());
                resolver.setChain(Arrays.asList(personDirectoryPrincipalResolver, new EchoingPrincipalResolver()));
                plan.registerAuthenticationHandlerWithPrincipalResolver(handler, resolver);
            });
        }
    }
}
