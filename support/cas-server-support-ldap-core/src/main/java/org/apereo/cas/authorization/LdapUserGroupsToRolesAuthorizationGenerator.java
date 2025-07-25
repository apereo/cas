package org.apereo.cas.authorization;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides a simple generator implementation that obtains user roles from an LDAP search.
 * Two searches are performed by this component for every user details lookup:
 * <ol>
 * <li>Search for an entry to resolve the username. In most cases the search should return exactly one result,
 * but the {@link #isAllowMultipleResults()} property may be toggled to change that behavior.</li>
 * <li>Search for groups of which the user is a member. This search commonly occurs on a separate directory
 * branch than that of the user search.</li>
 * </ol>
 *
 * @author Jerome Leleu
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class LdapUserGroupsToRolesAuthorizationGenerator extends BaseUseAttributesAuthorizationGenerator {

    private final String groupAttributeName;

    private final String groupPrefix;

    private final SearchOperation groupSearchOperation;

    public LdapUserGroupsToRolesAuthorizationGenerator(final SearchOperation userSearchOperation,
                                                       final boolean allowMultipleResults,
                                                       final String groupAttributeName,
                                                       final String groupPrefix,
                                                       final SearchOperation groupSearchOperation) {
        super(userSearchOperation, allowMultipleResults);
        this.groupAttributeName = groupAttributeName;
        this.groupPrefix = groupPrefix;
        this.groupSearchOperation = groupSearchOperation;
    }

    @Override
    protected List<SimpleGrantedAuthority> generateAuthorizationForLdapEntry(final Principal profile, final LdapEntry userEntry) {
        LOGGER.debug("Attempting to get roles for user [{}].", userEntry.getDn());
        val response = FunctionUtils.doUnchecked(() -> groupSearchOperation.execute(
            LdapUtils.newLdaptiveSearchFilter(groupSearchOperation.getTemplate().getFilter(),
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, CollectionUtils.wrap(userEntry.getDn()))));
        LOGGER.debug("LDAP role search response: [{}]", response);
        return response
            .getEntries()
            .stream()
            .map(entry -> entry.getAttribute(groupAttributeName))
            .filter(Objects::nonNull)
            .map(attribute -> attribute
                .getStringValues()
                .stream()
                .map(entry -> entry.toUpperCase(Locale.ENGLISH))
                .map(role -> Strings.CI.prependIfMissing(role, groupPrefix))
                .collect(Collectors.toList()))
            .flatMap(List::stream)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
}
