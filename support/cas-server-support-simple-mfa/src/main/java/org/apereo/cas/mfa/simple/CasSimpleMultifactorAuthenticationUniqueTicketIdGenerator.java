package org.apereo.cas.mfa.simple;

import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;

/**
 * This is {@link CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator implements UniqueTicketIdGenerator {
    private static final int TOKEN_LENGTH = 6;

    @Override
    public String getNewTicketId(final String prefix) {
        return prefix + '-' + RandomUtils.randomNumeric(TOKEN_LENGTH);
    }
}
