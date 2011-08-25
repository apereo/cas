package org.jasig.cas.adaptors.ldappwd.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasig.cas.adaptors.ldappwd.util.exceptions.BadHoursException;
import org.jasig.cas.authentication.handler.AuthenticationException;

/**
 * Ldap Error Code Processor : login outside allowed hours processor 
 * 
 * @author Philippe MARASSE
 */
public final class BadHoursErrorProcessor extends AbstractLdapErrorDetailProcessor {

    private final Pattern pattern = Pattern.compile(BadHoursException.BAD_HOURS_ERROR_REGEX);

    @Override
    boolean processErrorDetailInternal(String in_detail) throws AuthenticationException {

        final Matcher matcher = pattern.matcher(in_detail);
        if (matcher.find()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Pattern matches : throwing BadHoursException");
            }
            throw new BadHoursException();
        }
        return false;
    }

    @Override
    String processTicketExceptionCodeInternal(String in_code) {

        if (in_code.equals(BadHoursException.BAD_HOURS_CODE)) {
            return "showBadHoursView";
        }
        return null;
    }

}
