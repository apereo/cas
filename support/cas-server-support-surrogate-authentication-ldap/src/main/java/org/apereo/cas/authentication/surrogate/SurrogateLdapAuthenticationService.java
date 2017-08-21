package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RegexUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link SurrogateLdapAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateLdapAuthenticationService extends BaseSurrogateAuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateLdapAuthenticationService.class);
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
    public boolean canAuthenticateAsInternal(final String surrogate, final Principal principal, final Service service) {
        try {
            if (surrogate.equalsIgnoreCase(principal.getId())) {
                return true;
            }

            final SearchFilter filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSurrogateSearchFilter(), CollectionUtils.wrap(surrogate));
            LOGGER.debug("Using search filter: [{}]", filter);

            final Response<SearchResult> response = LdapUtils.executeSearchOperation(this.connectionFactory,
                    ldapProperties.getBaseDn(), filter);
            LOGGER.debug("LDAP response: [{}]", response);
            return LdapUtils.containsResultEntry(response);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        final Collection<String> eligible = new LinkedHashSet<>();
        try {
            final SearchFilter filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSearchFilter(), CollectionUtils.wrap(username));
            LOGGER.debug("Using search filter: [{}]", filter);

            final Response<SearchResult> response = LdapUtils.executeSearchOperation(this.connectionFactory,
                    ldapProperties.getBaseDn(), filter);
            LOGGER.debug("LDAP response: [{}]", response);

            if (!LdapUtils.containsResultEntry(response)) {
                return eligible;
            }

            final LdapEntry ldapEntry = response.getResult().getEntry();
            final LdapAttribute attribute = ldapEntry.getAttribute(ldapProperties.getMemberAttributeName());
            if (attribute == null || attribute.getStringValues().isEmpty()) {
                return eligible;
            }

            final Pattern pattern = RegexUtils.createPattern(ldapProperties.getMemberAttributeValueRegex());
            eligible.addAll(
                    attribute.getStringValues()
                            .stream()
                            .map(pattern::matcher)
                            .filter(Matcher::matches)
                            .map(p -> p.group(1))
                            .collect(Collectors.toList()));

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return eligible;
    }
}
