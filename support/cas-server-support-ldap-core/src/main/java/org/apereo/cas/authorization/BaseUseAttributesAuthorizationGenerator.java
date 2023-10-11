package org.apereo.cas.authorization;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.function.Function;

/**
 * This is {@link BaseUseAttributesAuthorizationGenerator}.
 *
 * <ol>
 * <li>Search for an entry to resolve the username. In most cases the search should return exactly one result,
 * but the {@link #allowMultipleResults} property may be toggled to change that behavior.</li>
 * </ol>
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseUseAttributesAuthorizationGenerator implements Function<Principal, List<SimpleGrantedAuthority>> {

    private final SearchOperation userSearchOperation;
    private final boolean allowMultipleResults;

    @Override
    public List<SimpleGrantedAuthority> apply(final Principal profile) {
        val username = profile.getId();
        LOGGER.debug("Attempting to get details for user [{}].", username);
        val filter = LdapUtils.newLdaptiveSearchFilter(userSearchOperation.getTemplate().getFilter(),
            LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, CollectionUtils.wrap(username));
        val response = FunctionUtils.doUnchecked(() -> userSearchOperation.execute(filter));

        LOGGER.debug("LDAP user search response: [{}]", response);
        if (!allowMultipleResults && response.entrySize() > 1) {
            throw new IllegalStateException("Found multiple results for user which is not allowed.");
        }

        if (response.entrySize() > 0) {
            val userEntry = response.getEntry();
            return generateAuthorizationForLdapEntry(profile, userEntry);
        }
        return List.of();
    }

    protected abstract List<SimpleGrantedAuthority> generateAuthorizationForLdapEntry(Principal profile, LdapEntry userEntry);
}
