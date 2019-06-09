package org.apereo.cas.pm.impl.history;

import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.util.DigestUtils;

/**
 * This is {@link BasePasswordHistoryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public abstract class BasePasswordHistoryService implements PasswordHistoryService {
    /**
     * Encode password string.
     *
     * @param password the password
     * @return the string
     */
    protected String encodePassword(final String password) {
        return DigestUtils.sha512(password);
    }
}
