package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collection;
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

    private final ConnectionFactory connectionFactory;
    private final SurrogateAuthenticationProperties.Ldap ldapProperties;

    public SurrogateLdapAuthenticationService(final ConnectionFactory connectionFactory,
                                              final SurrogateAuthenticationProperties.Ldap ldap,
                                              final ServicesManager servicesManager) {
        super(servicesManager);
        this.connectionFactory = connectionFactory;
        this.ldapProperties = ldap;
    }

    @Override
    public boolean canAuthenticateAsInternal(final String surrogate, final Principal principal, final Optional<Service> service) {
        try {
            val id = principal.getId();
            if (surrogate.equalsIgnoreCase(id)) {
                return true;
            }
            val filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSurrogateSearchFilter(),
                CollectionUtils.wrapList(LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, "surrogate"),
                CollectionUtils.wrapList(id, surrogate));
            LOGGER.debug("Using search filter to locate surrogate accounts for [{}]: [{}]", id, filter);

            val response = LdapUtils.executeSearchOperation(this.connectionFactory, ldapProperties.getBaseDn(), filter, ldapProperties.getPageSize());
            LOGGER.debug("LDAP response: [{}]", response);
            return LdapUtils.containsResultEntry(response);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        try {
            val filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSearchFilter(), CollectionUtils.wrap(username));
            LOGGER.debug("Using search filter to find eligible accounts: [{}]", filter);

            val response = LdapUtils.executeSearchOperation(this.connectionFactory, ldapProperties.getBaseDn(), filter, ldapProperties.getPageSize());
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
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.debug("No accounts may be eligible for surrogate authentication");
        return new ArrayList<>(0);
    }

    @Override
    public void destroy() {
        connectionFactory.close();
    }
}
