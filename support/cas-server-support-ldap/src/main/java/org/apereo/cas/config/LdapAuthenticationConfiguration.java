package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.support.DefaultAccountStateHandler;
import org.apereo.cas.authentication.support.LdapPasswordPolicyConfiguration;
import org.apereo.cas.authentication.support.OptionalWarningAccountStateHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

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
    @Qualifier("attributeRepositories")
    private List<IPersonAttributeDao> attributeRepositories;

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
                    final Map<String, String> attributes = Beans.transformPrincipalAttributesListIntoMap(l.getPrincipalAttributeList());
                    LOGGER.debug("Created and mapped principal attributes [{}] for [{}]...", attributes, l.getLdapUrl());

                    LOGGER.debug("Creating ldap authenticator for [{}] and baseDn [{}]", l.getLdapUrl(), l.getBaseDn());
                    final Authenticator authenticator = Beans.newLdaptiveAuthenticator(l);
                    LOGGER.debug("Ldap authenticator configured with return attributes [{}] for [{}] and baseDn [{}]",
                            attributes.keySet(), l.getLdapUrl(), l.getBaseDn());

                    LOGGER.debug("Creating ldap authentication handler for [{}]", l.getLdapUrl());
                    final LdapAuthenticationHandler handler = new LdapAuthenticationHandler(l.getName(),
                            servicesManager, ldapPrincipalFactory(),
                            l.getOrder(), authenticator);

                    final List<String> additionalAttributes = l.getAdditionalAttributes();
                    if (StringUtils.isNotBlank(l.getPrincipalAttributeId())) {
                        additionalAttributes.add(l.getPrincipalAttributeId());
                    }
                    handler.setAllowMultiplePrincipalAttributeValues(l.isAllowMultiplePrincipalAttributeValues());
                    handler.setAllowMissingPrincipalAttributeValue(l.isAllowMissingPrincipalAttributeValue());
                    handler.setPasswordEncoder(Beans.newPasswordEncoder(l.getPasswordEncoder()));
                    handler.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(l.getPrincipalTransformation()));

                    if (StringUtils.isNotBlank(l.getCredentialCriteria())) {
                        LOGGER.debug("Ldap authentication for [{}] is filtering credentials by [{}]",
                                l.getLdapUrl(), l.getCredentialCriteria());
                        handler.setCredentialSelectionPredicate(Beans.newCredentialSelectionPredicate(l.getCredentialCriteria()));
                    }

                    if (StringUtils.isBlank(l.getPrincipalAttributeId())) {
                        LOGGER.debug("No principal id attribute is found for ldap authentication via [{}]", l.getLdapUrl());
                    } else {
                        handler.setPrincipalIdAttribute(l.getPrincipalAttributeId());
                        LOGGER.debug("Using principal id attribute [{}] for ldap authentication via [{}]", l.getPrincipalAttributeId(),
                                l.getLdapUrl());
                    }

                    if (l.getPasswordPolicy().isEnabled()) {
                        LOGGER.debug("Password policy is enabled for [{}]. Constructing password policy configuration", l.getLdapUrl());
                        handler.setPasswordPolicyConfiguration(createLdapPasswordPolicyConfiguration(l, authenticator, attributes));
                    }

                    handler.setPrincipalAttributeMap(attributes);

                    LOGGER.debug("Initializing ldap authentication handler for [{}]", l.getLdapUrl());
                    handler.initialize();
                    handlers.add(handler);
                });
        return handlers;
    }


    private static Predicate<LdapAuthenticationProperties> ldapInstanceConfigurationPredicate() {
        return l -> {
            if (l.getType() == null) {
                LOGGER.warn("Skipping LDAP authentication entry since no type is defined");
                return false;
            }
            if (StringUtils.isBlank(l.getLdapUrl())) {
                LOGGER.warn("Skipping LDAP authentication entry since no ldap url is defined");
                return false;
            }
            return true;
        };
    }

    private static LdapPasswordPolicyConfiguration createLdapPasswordPolicyConfiguration(final LdapAuthenticationProperties l,
                                                                                         final Authenticator authenticator,
                                                                                         final Map<String, String> attributes) {
        final LdapPasswordPolicyConfiguration cfg = new LdapPasswordPolicyConfiguration(l.getPasswordPolicy());
        final Set<AuthenticationResponseHandler> handlers = new HashSet<>();
            LOGGER.debug("Password policy authentication response handler is set to accommodate directory type: [{}]",
                l.getPasswordPolicy().getType());
        switch (l.getPasswordPolicy().getType()) {
            case AD:
                handlers.add(new ActiveDirectoryAuthenticationResponseHandler(Period.ofDays(cfg.getPasswordWarningNumberOfDays())));
                Arrays.stream(ActiveDirectoryAuthenticationResponseHandler.ATTRIBUTES).forEach(a -> {
                    LOGGER.debug("Configuring authentication to retrieve password policy attribute [{}]", a);
                    attributes.put(a, a);
                });
                break;
            case FreeIPA:
                Arrays.stream(FreeIPAAuthenticationResponseHandler.ATTRIBUTES).forEach(a -> {
                    LOGGER.debug("Configuring authentication to retrieve password policy attribute [{}]", a);
                    attributes.put(a, a);
                });
                handlers.add(new FreeIPAAuthenticationResponseHandler(
                        Period.ofDays(cfg.getPasswordWarningNumberOfDays()), cfg.getLoginFailures()));
                break;
            case EDirectory:
                Arrays.stream(EDirectoryAuthenticationResponseHandler.ATTRIBUTES).forEach(a -> {
                    LOGGER.debug("Configuring authentication to retrieve password policy attribute [{}]", a);
                    attributes.put(a, a);
                });
                handlers.add(new EDirectoryAuthenticationResponseHandler(Period.ofDays(cfg.getPasswordWarningNumberOfDays())));
                break;
            default:
                handlers.add(new PasswordPolicyAuthenticationResponseHandler());
                handlers.add(new PasswordExpirationAuthenticationResponseHandler());
                break;
        }
        authenticator.setAuthenticationResponseHandlers((AuthenticationResponseHandler[]) handlers.toArray(
                new AuthenticationResponseHandler[handlers.size()]));

        LOGGER.debug("LDAP authentication response handlers configured are: [{}]", handlers);

        if (StringUtils.isNotBlank(l.getPasswordPolicy().getWarningAttributeName())
                && StringUtils.isNotBlank(l.getPasswordPolicy().getWarningAttributeValue())) {

            final OptionalWarningAccountStateHandler accountHandler = new OptionalWarningAccountStateHandler();
            accountHandler.setDisplayWarningOnMatch(l.getPasswordPolicy().isDisplayWarningOnMatch());
            accountHandler.setWarnAttributeName(l.getPasswordPolicy().getWarningAttributeName());
            accountHandler.setWarningAttributeValue(l.getPasswordPolicy().getWarningAttributeValue());
            accountHandler.setAttributesToErrorMap(l.getPasswordPolicy().getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
            LOGGER.debug("Configuring an warning account state handler for LDAP authentication for warning attribute [{}] and value [{}]",
                    l.getPasswordPolicy().getWarningAttributeName(), l.getPasswordPolicy().getWarningAttributeValue());
        } else {
            final DefaultAccountStateHandler accountHandler = new DefaultAccountStateHandler();
            accountHandler.setAttributesToErrorMap(l.getPasswordPolicy().getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
            LOGGER.debug("Configuring the default account state handler for LDAP authentication");
        }
        return cfg;
    }


    /**
     * The type Ldap authentication event execution plan configuration.
     */
    @Configuration("ldapAuthenticationEventExecutionPlanConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class LdapAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {

        private boolean isAttributeRepositorySourceDefined() {
            return !attributeRepositories.isEmpty();
        }

        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            ldapAuthenticationHandlers().forEach(handler -> {
                final ChainingPrincipalResolver resolver = new ChainingPrincipalResolver();
                if (isAttributeRepositorySourceDefined()) {
                    LOGGER.debug("Attribute repository sources are defined and available for the principal resolution chain");
                    resolver.setChain(Arrays.asList(personDirectoryPrincipalResolver, new EchoingPrincipalResolver()));
                } else {
                    LOGGER.debug("Attribute repository sources are not available for principal resolution so principal resolver will echo "
                            + "back the principal resolved during LDAP authentication directly.");
                    resolver.setChain(new EchoingPrincipalResolver());
                }
                LOGGER.info("Ldap authentication for [{}] is to chain principal resolvers via [{}] for attribute resolution",
                        handler.getName(), resolver);
                plan.registerAuthenticationHandlerWithPrincipalResolver(handler, resolver);
            });
        }
    }
}
