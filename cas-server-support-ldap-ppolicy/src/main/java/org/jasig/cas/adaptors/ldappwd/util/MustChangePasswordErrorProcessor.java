package org.jasig.cas.adaptors.ldappwd.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.cas.adaptors.ldappwd.util.exceptions.MustChangePasswordException;
import org.jasig.cas.authentication.handler.AuthenticationException;

/**
 * Ldap Error Code Processor : Password mus be changed processor
 * 
 * @author Philippe MARASSE
 */
public final class MustChangePasswordErrorProcessor extends AbstractLdapErrorDetailProcessor {

    private final Pattern pattern = Pattern.compile(MustChangePasswordException.MUST_CHANGE_PASSWORD_ERROR_REGEX);

    @Override
    boolean processErrorDetailInternal(String in_detail) throws AuthenticationException {

        final Matcher matcher = pattern.matcher(in_detail);
        if (matcher.find()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Pattern matches : throwing MustChangePasswordException");
            }
            throw new MustChangePasswordException();
        }
        return false;
    }

    @Override
    String processTicketExceptionCodeInternal(String in_code) {

        if (in_code.equals(MustChangePasswordException.MUST_CHANGE_PASSWORD_CODE)) {
            return "showMustChangePassView";
        }
        return null;
    }

}
