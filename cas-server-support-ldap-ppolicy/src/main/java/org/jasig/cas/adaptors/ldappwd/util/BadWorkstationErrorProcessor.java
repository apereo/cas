package org.jasig.cas.adaptors.ldappwd.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.cas.adaptors.ldappwd.util.exceptions.BadWorkstationException;
import org.jasig.cas.authentication.handler.AuthenticationException;

/**
 * Ldap error processor : login from a bad workstation processor
 * 
 * @author Philippe MARASSE
 */
public final class BadWorkstationErrorProcessor extends AbstractLdapErrorDetailProcessor {

    private final Pattern pattern = Pattern.compile(BadWorkstationException.BAD_WORKSTATION_ERROR_REGEX);

    @Override
    boolean processErrorDetailInternal(String in_detail) throws AuthenticationException {

        final Matcher matcher = pattern.matcher(in_detail);
        if (matcher.find()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Pattern matches : throwing BadWorkstationException");
            }
            throw new BadWorkstationException();
        }
        return false;
    }

    @Override
    String processTicketExceptionCodeInternal(String in_code) {

        if (in_code.equals(BadWorkstationException.BAD_WORKSTATION_CODE)) {
            return "showBadWorkstationView";
        }
        return null;
    }

}
