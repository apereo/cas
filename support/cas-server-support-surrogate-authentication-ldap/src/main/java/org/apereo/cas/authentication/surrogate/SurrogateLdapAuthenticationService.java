package org.apereo.cas.authentication.surrogate;

import module java.base;
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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link SurrogateLdapAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SurrogateLdapAuthenticationService extends BaseSurrogateAuthenticationService implements DisposableBean {
    /**
     * Name of the filter parameter that carries the impersonated account.
     */
    private static final String SURROGATE_FILTER_PARAMETER = "surrogate";

    /**
     * Positional index of {@link #SURROGATE_FILTER_PARAMETER} in the surrogate search filter.
     */
    private static final int SURROGATE_FILTER_PARAMETER_INDEX = 1;

    private final Map<String, LdapConnectionFactory> connectionFactories;

    public SurrogateLdapAuthenticationService(final CasConfigurationProperties casProperties,
                                              final ServicesManager servicesManager,
                                              final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
                                              final ConfigurableApplicationContext applicationContext) {
        super(servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
        this.connectionFactories = new LinkedHashMap<>();
        casProperties.getAuthn().getSurrogate().getLdap()
            .forEach(ldap -> connectionFactories.computeIfAbsent(ldap.toStableIdentifier(),
                _ -> new LdapConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(ldap))));
    }

    @Override
    public void destroy() {
        connectionFactories.values().forEach(LdapConnectionFactory::close);
    }

    @Override
    public boolean isWildcardedAccount(final String surrogate, final Principal principal, final Optional<? extends Service> service) throws Throwable {
        return super.isWildcardedAccount(surrogate, principal, service) && doesSurrogateAccountExistInLdap(surrogate);
    }

    @Override
    public boolean canImpersonateInternal(final String surrogate, final Principal principal, final Optional<? extends Service> service) {
        for (val ldap : casProperties.getAuthn().getSurrogate().getLdap()) {
            try {
                val id = principal.getId();
                if (!LdapUtils.containsSearchFilterParameter(ldap.getSurrogateSearchFilter(),
                    SURROGATE_FILTER_PARAMETER, SURROGATE_FILTER_PARAMETER_INDEX)) {
                    LOGGER.error("Surrogate search filter [{}] defined for [{}] does not refer to the [{}] parameter. "
                                 + "The filter cannot restrict the accounts [{}] is allowed to impersonate and is ignored.",
                        ldap.getSurrogateSearchFilter(), ldap.getLdapUrl(), SURROGATE_FILTER_PARAMETER, id);
                    continue;
                }
                val searchFilter = LdapUtils.newLdaptiveSearchFilter(ldap.getSurrogateSearchFilter(),
                    CollectionUtils.wrapList(LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, SURROGATE_FILTER_PARAMETER),
                    CollectionUtils.wrapList(id, surrogate));
                LOGGER.debug("Using LDAP search filter [{}] to authorize principal [{}] to impersonate [{}]", searchFilter, id, surrogate);
                val response = connectionFactoryFor(ldap).executeSearchOperation(ldap.getBaseDn(), searchFilter, ldap.getPageSize());
                LOGGER.debug("LDAP search response: [{}]", response);
                if (LdapUtils.containsResultEntry(response) && doesSurrogateAccountExistInLdap(surrogate)) {
                    return true;
                }
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        return false;
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username, final Optional<? extends Service> service) {
        for (val ldap : casProperties.getAuthn().getSurrogate().getLdap()) {
            try {
                val filter = LdapUtils.newLdaptiveSearchFilter(ldap.getSearchFilter(), CollectionUtils.wrap(username));
                LOGGER.debug("Using search filter to find eligible accounts: [{}]", filter);

                val response = connectionFactoryFor(ldap).executeSearchOperation(ldap.getBaseDn(), filter, ldap.getPageSize());
                LOGGER.debug("LDAP response: [{}]", response);

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

    protected boolean doesSurrogateAccountExistInLdap(final String surrogate,
                                                      final LdapConnectionFactory connectionFactory,
                                                      final SurrogateLdapAuthenticationProperties ldap) throws Throwable {
        if (StringUtils.isBlank(ldap.getSurrogateValidationFilter())) {
            return true;
        }
        val validationFilter = LdapUtils.newLdaptiveSearchFilter(ldap.getSurrogateValidationFilter(),
            SURROGATE_FILTER_PARAMETER, List.of(surrogate));
        LOGGER.debug("Using surrogate validation filter [{}] to verify surrogate account [{}]", validationFilter, surrogate);
        val response = connectionFactory.executeSearchOperation(ldap.getBaseDn(), validationFilter, ldap.getPageSize());
        LOGGER.debug("LDAP validation response: [{}]", response);
        return LdapUtils.containsResultEntry(response);
    }

    protected boolean doesSurrogateAccountExistInLdap(final String surrogate) {
        for (val ldap : casProperties.getAuthn().getSurrogate().getLdap()) {
            try {
                if (doesSurrogateAccountExistInLdap(surrogate, connectionFactoryFor(ldap), ldap)) {
                    return true;
                }
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        return false;
    }

    private LdapConnectionFactory connectionFactoryFor(final SurrogateLdapAuthenticationProperties ldap) {
        return Objects.requireNonNull(connectionFactories.get(ldap.toStableIdentifier()));
    }
}
