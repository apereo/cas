package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateLdapAuthenticationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * This is {@link SurrogateLdapAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SurrogateLdapAuthenticationService extends BaseSurrogateAuthenticationService {

    public SurrogateLdapAuthenticationService(final CasConfigurationProperties casProperties,
                                              final ServicesManager servicesManager,
                                              final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
                                              final ConfigurableApplicationContext applicationContext) {
        super(servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
    }

    @Override
    public boolean isWildcardedAccount(final String surrogate, final Principal principal, final Optional<? extends Service> service) throws Throwable {
        return super.isWildcardedAccount(surrogate, principal, service) && doesSurrogateAccountExistInLdap(surrogate);
    }

    @Override
    public boolean canImpersonateInternal(final String surrogate, final Principal principal, final Optional<? extends Service> service) {
        boolean canImpersonate = false;
        try {
            canImpersonate = canImpersonateInLdap(principal, surrogate);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        boolean surrogateExists = false;
        if (canImpersonate) {
            try {
                surrogateExists = doesSurrogateAccountExistInLdap(surrogate);
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        return surrogateExists;
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username, final Optional<? extends Service> service) {
        val ldapProperties = casProperties.getAuthn().getSurrogate().getLdap();
        for (val ldap : ldapProperties) {
            try (val connectionFactory = new LdapConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap))) {
                val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(), CollectionUtils.wrap(username));
                LOGGER.debug("Using search filter to find eligible accounts: [{}]", filter);

                val response = connectionFactory.executeSearchOperation(ldap.getBaseDn(), filter, ldap.getPageSize());
                LOGGER.trace("LDAP response: [{}]", response);

                if (!LdapUtils.containsResultEntry(response)) {
                    LOGGER.warn("LDAP response is not found or does not contain a result entry for [{}]", username);
                    return new ArrayList<>();
                }

                val ldapEntry = response.getEntry();
                val attribute = ldapEntry.getAttribute(ldap.getMemberAttributeName());
                LOGGER.debug("Locating LDAP entry [{}] with attribute [{}]", ldapEntry, attribute);

                if (attribute == null || attribute.getStringValues().isEmpty()) {
                    LOGGER.warn("Attribute [{}] not found or has no values", ldap.getMemberAttributeName());
                    return new ArrayList<>();
                }

                val pattern = RegexUtils.createPattern(ldap.getMemberAttributeValueRegex());
                LOGGER.debug("Constructed attribute value regex pattern [{}]", pattern.pattern());
                val eligible = attribute.getStringValues()
                    .stream()
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .map(p -> {
                        if (p.groupCount() > 0) {
                            return p.group(1);
                        }
                        return p.group();
                    })
                    .sorted()
                    .collect(Collectors.toList());
                LOGGER.debug("Following accounts may be eligible for surrogate authentication: [{}]", eligible);
                return eligible;
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        LOGGER.debug("No accounts may be eligible for surrogate authentication");
        return new ArrayList<>();
    }

    protected boolean canImpersonateInLdap(final Principal principal,
                                           final String surrogate,
                                           final LdapConnectionFactory connectionFactory,
                                           final SurrogateLdapAuthenticationProperties ldap) throws Throwable {
        val id = principal.getId();
        val searchFilter = LdapUtils.newLdaptiveSearchFilter(ldap.getSurrogateSearchFilter(),
                CollectionUtils.wrapList(LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, "surrogate"),
                CollectionUtils.wrapList(id, surrogate));
        LOGGER.debug("Using LDAP search filter [{}] to authorize principal [{}] to impersonate [{}]", searchFilter, id, surrogate);
        var response = connectionFactory.executeSearchOperation(ldap.getBaseDn(), searchFilter, ldap.getPageSize());
        LOGGER.trace("LDAP search response: [{}]", response);
        return LdapUtils.containsResultEntry(response);
    }

    protected boolean canImpersonateInLdap(final Principal principal, final String surrogate) throws Throwable {
        val ldapProperties = casProperties.getAuthn().getSurrogate().getLdap();
        for (val ldap : ldapProperties) {
            try (val connectionFactory = new LdapConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap))) {
                if (canImpersonateInLdap(principal, surrogate, connectionFactory, ldap)) {
                    return true;
                }
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        return false;
    }

    protected boolean doesSurrogateAccountExistInLdap(final String surrogate,
                                                      final LdapConnectionFactory connectionFactory,
                                                      final SurrogateLdapAuthenticationProperties ldap) throws Throwable {
        if (StringUtils.isBlank(ldap.getSurrogateValidationFilter())) {
            return true;
        }
        val validationFilter = LdapUtils.newLdaptiveSearchFilter(ldap.getSurrogateValidationFilter(),
            "surrogate", List.of(surrogate));
        LOGGER.debug("Using surrogate validation filter [{}] to verify surrogate account [{}]", validationFilter, surrogate);
        val response = connectionFactory.executeSearchOperation(ldap.getBaseDn(), validationFilter, ldap.getPageSize());
        LOGGER.trace("LDAP validation response: [{}]", response);
        return LdapUtils.containsResultEntry(response);
    }

    protected boolean doesSurrogateAccountExistInLdap(final String surrogate) {
        val ldapProperties = casProperties.getAuthn().getSurrogate().getLdap();
        for (val ldap : ldapProperties) {
            try (val connectionFactory = new LdapConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap))) {
                if (doesSurrogateAccountExistInLdap(surrogate, connectionFactory, ldap)) {
                    return true;
                }
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        return false;
    }
}
