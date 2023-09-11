package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateLdapAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.DisposableBean;

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
public class SurrogateLdapAuthenticationService extends BaseSurrogateAuthenticationService implements DisposableBean {

    private final LdapConnectionFactory connectionFactory;

    private final SurrogateLdapAuthenticationProperties ldapProperties;

    public SurrogateLdapAuthenticationService(final ConnectionFactory connectionFactory,
                                              final SurrogateLdapAuthenticationProperties ldap,
                                              final ServicesManager servicesManager) {
        super(servicesManager);
        this.connectionFactory = new LdapConnectionFactory(connectionFactory);
        this.ldapProperties = ldap;
    }

    @Override
    public boolean isWildcardedAccount(final String surrogate, final Principal principal) throws Throwable {
        return super.isWildcardedAccount(surrogate, principal)
            && doesSurrogateAccountExistInLdap(surrogate);
    }

    @Override
    public boolean canImpersonateInternal(final String surrogate, final Principal principal, final Optional<Service> service) {
        try {
            val id = principal.getId();
            val searchFilter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSurrogateSearchFilter(),
                CollectionUtils.wrapList(LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, "surrogate"),
                CollectionUtils.wrapList(id, surrogate));
            LOGGER.debug("Using LDAP search filter [{}] to authorize principal [{}] to impersonate [{}]", searchFilter, id, surrogate);
            var response = connectionFactory.executeSearchOperation(ldapProperties.getBaseDn(), searchFilter, ldapProperties.getPageSize());
            LOGGER.debug("LDAP search response: [{}]", response);
            var entryResult = LdapUtils.containsResultEntry(response);
            if (entryResult && StringUtils.isNotBlank(ldapProperties.getSurrogateValidationFilter())) {
                entryResult = doesSurrogateAccountExistInLdap(surrogate);
            }
            return entryResult;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    private boolean doesSurrogateAccountExistInLdap(final String surrogate) {
        return FunctionUtils.doUnchecked(() -> {
            val validationFilter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSurrogateValidationFilter(),
                "surrogate", List.of(surrogate));
            LOGGER.debug("Using surrogate validation filter [{}] to verify surrogate account [{}]", validationFilter, surrogate);
            val response = connectionFactory.executeSearchOperation(ldapProperties.getBaseDn(), validationFilter, ldapProperties.getPageSize());
            LOGGER.debug("LDAP validation response: [{}]", response);
            return LdapUtils.containsResultEntry(response);
        });
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username) {
        try {
            val filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSearchFilter(), CollectionUtils.wrap(username));
            LOGGER.debug("Using search filter to find eligible accounts: [{}]", filter);

            val response = connectionFactory.executeSearchOperation(ldapProperties.getBaseDn(),
                filter, ldapProperties.getPageSize());
            LOGGER.debug("LDAP response: [{}]", response);

            if (!LdapUtils.containsResultEntry(response)) {
                LOGGER.warn("LDAP response is not found or does not contain a result entry for [{}]", username);
                return new ArrayList<>(0);
            }

            val ldapEntry = response.getEntry();
            val attribute = ldapEntry.getAttribute(ldapProperties.getMemberAttributeName());
            LOGGER.debug("Locating LDAP entry [{}] with attribute [{}]", ldapEntry, attribute);

            if (attribute == null || attribute.getStringValues().isEmpty()) {
                LOGGER.warn("Attribute [{}] not found or has no values", ldapProperties.getMemberAttributeName());
                return new ArrayList<>(0);
            }

            val pattern = RegexUtils.createPattern(ldapProperties.getMemberAttributeValueRegex());
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
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }

        LOGGER.debug("No accounts may be eligible for surrogate authentication");
        return new ArrayList<>(0);
    }

    @Override
    public void destroy() {
        connectionFactory.close();
    }
}
