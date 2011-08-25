package org.jasig.cas.adaptors.ldappwd.util.exceptions;

import org.jasig.cas.authentication.handler.AuthenticationException;

public final class AccountDisabledException extends AuthenticationException {

    private static final long serialVersionUID = -5838105609896634659L;

    /**
     * Spring uses this code to map the locale specific error message from properties files in WEB-INF/classes/.
     */
    public static final String ACCOUNT_DISABLED_CODE = "error.authentication.account.disabled";

    /**
     * Regex to match Active Directory (and other LDAP directories) error code for an account being locked
     */
    public static final String ACCOUNT_DISABLED_ERROR_REGEX = "\\D533\\D|\\D701\\D|\\D53\\D|Account inactivated|OperationNotSupportedException";

    public AccountDisabledException() {
        super(AccountDisabledException.ACCOUNT_DISABLED_CODE);
    }

    public AccountDisabledException(final String code) {
        super(code);
    }
}
