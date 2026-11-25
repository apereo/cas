package org.apereo.cas.palantir;

import module java.base;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Who may do what in Palantir, by the authorities of the person signed in.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class PalantirAuthorities {
    /**
     * The authority of Palantir's administrators.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * Whether the person signed in is an administrator of Palantir, who sees and does everything it offers.
     *
     * @param authentication who is signed in, if anyone
     * @return true when they hold the administrator authority
     */
    public static boolean isAdministrator(final @Nullable Authentication authentication) {
        return authentication != null && authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(ROLE_ADMIN::equals);
    }
}
