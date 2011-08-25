package org.jasig.cas.adaptors.ldappwd.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.cas.adaptors.ldappwd.util.exceptions.AccountDisabledException;

/**
 * Ldap Error Code Processor : Disabled Account processor
 * 
 * @author Philippe MARASSE
 */
public final class AccountDisabledErrorProcessor extends AbstractLdapErrorDetailProcessor {

    private final Pattern pattern = Pattern.compile(AccountDisabledException.ACCOUNT_DISABLED_ERROR_REGEX);

    @Override
    boolean processErrorDetailInternal(String in_detail) throws AccountDisabledException {
        final Matcher matcher = pattern.matcher(in_detail);
        if (matcher.find()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Pattern matches : throwing AccountDisabledException");
            }
            throw new AccountDisabledException();
        }
        return false;
    }

    @Override
    String processTicketExceptionCodeInternal(String in_code) {
        if (in_code.equals(AccountDisabledException.ACCOUNT_DISABLED_CODE)) {
            return "showAccountDisabledView";
        }
        return null;
    }

}
