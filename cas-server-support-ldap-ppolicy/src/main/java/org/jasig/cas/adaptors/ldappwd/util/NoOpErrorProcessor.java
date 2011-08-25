package org.jasig.cas.adaptors.ldappwd.util;

import org.jasig.cas.authentication.handler.AuthenticationException;

/**
 * Ldap Error Processor that does nothing at all
 * 
 * @author Philippe MARASSE
 */
public final class NoOpErrorProcessor extends AbstractLdapErrorDetailProcessor {

    @Override
    boolean processErrorDetailInternal(String in_detail) throws AuthenticationException {

        return true;
    }

    @Override
    String processTicketExceptionCodeInternal(String in_code) {

        return null;
    }

}
