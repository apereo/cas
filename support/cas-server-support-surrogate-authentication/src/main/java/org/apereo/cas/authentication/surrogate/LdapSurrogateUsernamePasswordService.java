package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.support.Beans;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link LdapSurrogateUsernamePasswordService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class LdapSurrogateUsernamePasswordService implements SurrogateAuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapSurrogateUsernamePasswordService.class);
    private final ConnectionFactory connectionFactory;
    private final String baseDn;
    private final String memberAttributeName;
    private final Pattern memberAttributeValueRegex;
    private final String searchFilter;

    public LdapSurrogateUsernamePasswordService(final ConnectionFactory connectionFactory,
                                                final String baseDn, final String memberAttribute,
                                                final String memberAttributeValueRegex, final String searchFilter) {
        this.connectionFactory = connectionFactory;
        this.baseDn = baseDn;
        this.memberAttributeName = memberAttribute;
        this.memberAttributeValueRegex = RegexUtils.createPattern(memberAttributeValueRegex);
        this.searchFilter = searchFilter;
    }

    @Override
    public boolean canAuthenticateAs(final String username, final Principal surrogate) {
        try {
            if (username.equalsIgnoreCase(surrogate.getId())) {
                return true;
            }
            final SearchFilter filter = Beans.newLdaptiveSearchFilter(this.searchFilter, Arrays.asList(username));
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(this.connectionFactory, baseDn, filter);
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
            final SearchFilter filter = Beans.newLdaptiveSearchFilter(this.searchFilter, Arrays.asList(username));
            final Response<SearchResult> response = LdapUtils.executeSearchOperation(this.connectionFactory, baseDn, filter);
            LOGGER.debug("LDAP response: [{}]", response);

            if (!LdapUtils.containsResultEntry(response)) {
                return eligible;
            }

            final LdapEntry ldapEntry = response.getResult().getEntry();
            final LdapAttribute attribute = ldapEntry.getAttribute(this.memberAttributeName);
            if (attribute == null || attribute.getStringValues().isEmpty()) {
                return eligible;
            }
            eligible.addAll(
                    attribute.getStringValues()
                            .stream()
                            .map(this.memberAttributeValueRegex::matcher)
                            .filter(Matcher::matches)
                            .map(p -> p.group(1))
                            .collect(Collectors.toList()));

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return eligible;
    }
}
