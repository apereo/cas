package org.jasig.cas.adaptors.ldappwd.util.exceptions;

import org.jasig.cas.authentication.handler.AuthenticationException;

public final class AccountLockedException extends AuthenticationException {

    private static final long serialVersionUID = -4189558977778418141L;

    /**
     * Spring uses this code to map the locale specific error message from the messages properties files in WEB-INF/classes/.
     */
    public static final String ACCOUNT_LOCKED_CODE = "error.authentication.account.locked";

    // Active Directory error code for an account being locked

    public static final String ACCOUNT_LOCKED_ERROR_REGEX = "\\D775\\D|\\D19\\D|password retry";

    public AccountLockedException() {
        super(AccountLockedException.ACCOUNT_LOCKED_CODE);
    }

    public AccountLockedException(final String code) {
        super(code);
    }
}
