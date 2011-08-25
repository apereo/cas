package org.jasig.cas.adaptors.ldappwd.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.cas.adaptors.ldappwd.util.exceptions.ExpiredPasswordException;
import org.jasig.cas.authentication.handler.AuthenticationException;

/**
 * Ldap error processor : Password expired processor
 * 
 * @author Philippe MARASSE
 */
public final class ExpiredPasswordErrorProcessor extends AbstractLdapErrorDetailProcessor {

    private final Pattern pattern = Pattern.compile(ExpiredPasswordException.EXPIRED_PASSWORD_ERROR_REGEX);
    
    @Override
    boolean processErrorDetailInternal(String in_detail) throws AuthenticationException {
        final Matcher matcher = pattern.matcher(in_detail);
        if (matcher.find()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Pattern matches : throwing ExpiredPasswordException");
            }
            throw new ExpiredPasswordException();
        }
        return false;
    }

    @Override
    String processTicketExceptionCodeInternal(String in_code) {
        if (in_code.equals(ExpiredPasswordException.EXPIRED_PASSWORD_CODE)) {
            return "showExpiredPassView";
        }
        return null;
    }

}
