package org.apereo.cas.config;

import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.DefaultLdapAccountStateHandler;
import org.apereo.cas.authentication.support.password.DefaultPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.OptionalWarningLdapAccountStateHandler;
import org.apereo.cas.authentication.support.RejectResultCodeLdapPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapPasswordPolicyProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.time.Period;
import java.util.ArrayList;
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
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("ldapAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class LdapAuthenticationConfiguration {


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
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    public Collection<AuthenticationHandler> ldapAuthenticationHandlers() {
        final Collection<AuthenticationHandler> handlers = new HashSet<>();

        casProperties.getAuthn().getLdap()
            .stream()
            .filter(ldapInstanceConfigurationPredicate())
            .forEach(l -> {
                final Multimap<String, Object> multiMapAttributes =
                    CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(l.getPrincipalAttributeList());
                LOGGER.debug("Created and mapped principal attributes [{}] for [{}]...", multiMapAttributes, l.getLdapUrl());

                LOGGER.debug("Creating LDAP authenticator for [{}] and baseDn [{}]", l.getLdapUrl(), l.getBaseDn());
                final Authenticator authenticator = LdapUtils.newLdaptiveAuthenticator(l);
                LOGGER.debug("Ldap authenticator configured with return attributes [{}] for [{}] and baseDn [{}]",
                    multiMapAttributes.keySet(), l.getLdapUrl(), l.getBaseDn());

                LOGGER.debug("Creating LDAP password policy handling strategy for [{}]", l.getLdapUrl());
                final AuthenticationPasswordPolicyHandlingStrategy strategy = createLdapPasswordPolicyHandlingStrategy(l);

                LOGGER.debug("Creating LDAP authentication handler for [{}]", l.getLdapUrl());
                final LdapAuthenticationHandler handler = new LdapAuthenticationHandler(l.getName(),
                    servicesManager, ldapPrincipalFactory(), l.getOrder(), authenticator, strategy);
                handler.setCollectDnAttribute(l.isCollectDnAttribute());

                final List<String> additionalAttributes = l.getAdditionalAttributes();
                if (StringUtils.isNotBlank(l.getPrincipalAttributeId())) {
                    additionalAttributes.add(l.getPrincipalAttributeId());
                }
                if (StringUtils.isNotBlank(l.getPrincipalDnAttributeName())) {
                    handler.setPrincipalDnAttributeName(l.getPrincipalDnAttributeName());
                }
                handler.setAllowMultiplePrincipalAttributeValues(l.isAllowMultiplePrincipalAttributeValues());
                handler.setAllowMissingPrincipalAttributeValue(l.isAllowMissingPrincipalAttributeValue());
                handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(l.getPasswordEncoder()));
                handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(l.getPrincipalTransformation()));

                if (StringUtils.isNotBlank(l.getCredentialCriteria())) {
                    LOGGER.debug("Ldap authentication for [{}] is filtering credentials by [{}]",
                        l.getLdapUrl(), l.getCredentialCriteria());
                    handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(l.getCredentialCriteria()));
                }

                if (StringUtils.isBlank(l.getPrincipalAttributeId())) {
                    LOGGER.debug("No principal id attribute is found for LDAP authentication via [{}]", l.getLdapUrl());
                } else {
                    handler.setPrincipalIdAttribute(l.getPrincipalAttributeId());
                    LOGGER.debug("Using principal id attribute [{}] for LDAP authentication via [{}]", l.getPrincipalAttributeId(), l.getLdapUrl());
                }

                final LdapPasswordPolicyProperties passwordPolicy = l.getPasswordPolicy();
                if (passwordPolicy.isEnabled()) {
                    LOGGER.debug("Password policy is enabled for [{}]. Constructing password policy configuration", l.getLdapUrl());
                    final PasswordPolicyConfiguration cfg = createLdapPasswordPolicyConfiguration(passwordPolicy, authenticator, multiMapAttributes);
                    handler.setPasswordPolicyConfiguration(cfg);
                }

                final Map<String, Object> attributes = CollectionUtils.wrap(multiMapAttributes);
                handler.setPrincipalAttributeMap(attributes);

                LOGGER.debug("Initializing LDAP authentication handler for [{}]", l.getLdapUrl());
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
                LOGGER.warn("Skipping LDAP authentication entry since no LDAP url is defined");
                return false;
            }
            return true;
        };
    }

    private AuthenticationPasswordPolicyHandlingStrategy<AuthenticationResponse, PasswordPolicyConfiguration>
        createLdapPasswordPolicyHandlingStrategy(final LdapAuthenticationProperties l) {
        if (l.getPasswordPolicy().getStrategy() == LdapPasswordPolicyProperties.PasswordPolicyHandlingOptions.REJECT_RESULT_CODE) {
            LOGGER.debug("Created LDAP password policy handling strategy based on blacklisted authentication result codes");
            return new RejectResultCodeLdapPasswordPolicyHandlingStrategy();
        }

        final Resource location = l.getPasswordPolicy().getGroovy().getLocation();
        if (l.getPasswordPolicy().getStrategy() == LdapPasswordPolicyProperties.PasswordPolicyHandlingOptions.GROOVY && location != null) {
            LOGGER.debug("Created LDAP password policy handling strategy based on Groovy script [{}]", location);
            return new GroovyPasswordPolicyHandlingStrategy(location);
        }

        LOGGER.debug("Created default LDAP password policy handling strategy");
        return new DefaultPasswordPolicyHandlingStrategy();
    }

    private PasswordPolicyConfiguration createLdapPasswordPolicyConfiguration(final LdapPasswordPolicyProperties passwordPolicy,
                                                                              final Authenticator authenticator,
                                                                              final Multimap<String, Object> attributes) {
        final PasswordPolicyConfiguration cfg = new PasswordPolicyConfiguration(passwordPolicy);
        final Set<AuthenticationResponseHandler> handlers = new HashSet<>();

        final String customPolicyClass = passwordPolicy.getCustomPolicyClass();
        if (StringUtils.isNotBlank(customPolicyClass)) {
            try {
                LOGGER.debug("Configuration indicates use of a custom password policy handler [{}]", customPolicyClass);
                final Class<AuthenticationResponseHandler> clazz = (Class<AuthenticationResponseHandler>) Class.forName(customPolicyClass);
                handlers.add(clazz.getDeclaredConstructor().newInstance());
            } catch (final Exception e) {
                LOGGER.warn("Unable to construct an instance of the password policy handler", e);
            }
        }
        LOGGER.debug("Password policy authentication response handler is set to accommodate directory type: [{}]", passwordPolicy.getType());
        switch (passwordPolicy.getType()) {
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
        authenticator.setAuthenticationResponseHandlers((AuthenticationResponseHandler[]) handlers.toArray(new AuthenticationResponseHandler[0]));

        LOGGER.debug("LDAP authentication response handlers configured are: [{}]", handlers);

        if (!passwordPolicy.isAccountStateHandlingEnabled()) {
            cfg.setAccountStateHandler((response, configuration) -> new ArrayList<>(0));
            LOGGER.debug("Handling LDAP account states is disabled via CAS configuration");
        } else if (StringUtils.isNotBlank(passwordPolicy.getWarningAttributeName()) && StringUtils.isNotBlank(passwordPolicy.getWarningAttributeValue())) {
            final OptionalWarningLdapAccountStateHandler accountHandler = new OptionalWarningLdapAccountStateHandler();
            accountHandler.setDisplayWarningOnMatch(passwordPolicy.isDisplayWarningOnMatch());
            accountHandler.setWarnAttributeName(passwordPolicy.getWarningAttributeName());
            accountHandler.setWarningAttributeValue(passwordPolicy.getWarningAttributeValue());
            accountHandler.setAttributesToErrorMap(passwordPolicy.getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
            LOGGER.debug("Configuring an warning account state handler for LDAP authentication for warning attribute [{}] and value [{}]",
                passwordPolicy.getWarningAttributeName(), passwordPolicy.getWarningAttributeValue());
        } else {
            final DefaultLdapAccountStateHandler accountHandler = new DefaultLdapAccountStateHandler();
            accountHandler.setAttributesToErrorMap(passwordPolicy.getPolicyAttributes());
            cfg.setAccountStateHandler(accountHandler);
            LOGGER.debug("Configuring the default account state handler for LDAP authentication");
        }
        return cfg;
    }

    @ConditionalOnMissingBean(name = "ldapAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer ldapAuthenticationEventExecutionPlanConfigurer() {
        return plan -> ldapAuthenticationHandlers().forEach(handler -> {
            LOGGER.info("Registering LDAP authentication for [{}]", handler.getName());
            plan.registerAuthenticationHandlerWithPrincipalResolver(handler, personDirectoryPrincipalResolver);
        });
    }
}
