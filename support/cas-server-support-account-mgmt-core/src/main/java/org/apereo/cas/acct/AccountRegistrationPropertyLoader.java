package org.apereo.cas.acct;

import java.util.Map;

/**
 * This is {@link AccountRegistrationPropertyLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface AccountRegistrationPropertyLoader {
    /**
     * Load.
     *
     * @return the list
     */
    Map<String, AccountRegistrationProperty> load();

    /**
     * Store.
     *
     * @param map the map
     * @return the account registration property loader
     */
    AccountRegistrationPropertyLoader store(Map<String, AccountRegistrationProperty> map);
}
