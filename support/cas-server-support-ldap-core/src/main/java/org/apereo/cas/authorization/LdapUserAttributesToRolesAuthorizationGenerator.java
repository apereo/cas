package org.apereo.cas.authorization;

import org.apereo.cas.authentication.principal.Principal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides a simple generator implementation that obtains user roles from an LDAP search.
 * Searches are performed by this component for every user details lookup:
 *
 * @author Jerome Leleu
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Slf4j
@Getter
public class LdapUserAttributesToRolesAuthorizationGenerator extends BaseUseAttributesAuthorizationGenerator {

    private final String roleAttribute;

    private final String rolePrefix;

    public LdapUserAttributesToRolesAuthorizationGenerator(final SearchOperation userSearchOperation,
                                                           final boolean allowMultipleResults,
                                                           final String roleAttribute,
                                                           final String rolePrefix) {
        super(userSearchOperation, allowMultipleResults);
        this.roleAttribute = roleAttribute;
        this.rolePrefix = rolePrefix;
    }

    @Override
    protected List<SimpleGrantedAuthority> generateAuthorizationForLdapEntry(final Principal profile,
                                                                             final LdapEntry userEntry) {
        val attribute = userEntry.getAttribute(this.roleAttribute);
        return Optional.ofNullable(attribute)
            .map(LdapAttribute::getStringValues)
            .map(value -> value.stream()
                .map(entry -> entry.toUpperCase(Locale.ENGLISH))
                .map(role -> Strings.CI.prependIfMissing(role, rolePrefix))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()))
            .stream()
            .flatMap(List::stream)
            .toList();
    }
}
