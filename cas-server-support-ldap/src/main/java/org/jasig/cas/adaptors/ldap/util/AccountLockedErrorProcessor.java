package org.jasig.cas.adaptors.ldap.util;

import org.jasig.cas.adaptors.ldap.util.exceptions.AccountLockedException;
import org.jasig.cas.authentication.handler.AuthenticationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ldap Error Code Processor : Locked Account processor
 * 
 * @author Philippe MARASSE
 */
public final class AccountLockedErrorProcessor extends AbstractLdapErrorDetailProcessor {

    private final Pattern pattern = Pattern.compile(AccountLockedException.ACCOUNT_LOCKED_ERROR_REGEX);
    
    @Override
    boolean processErrorDetailInternal(String in_detail) throws AuthenticationException {
        final Matcher matcher = pattern.matcher(in_detail);
        if (matcher.find()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Pattern matches : throwing AccountLockedException");
            }
            throw new AccountLockedException();
        }
        return false;
    }

    @Override
    String processTicketExceptionCodeInternal(String in_code) {
        if (in_code.equals(AccountLockedException.ACCOUNT_LOCKED_CODE)) {
            return "showAccountLockedView";
        }
        return null;
    }

}
