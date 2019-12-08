package org.apereo.cas.mfa.simple;

import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;

import lombok.RequiredArgsConstructor;

/**
 * This is {@link CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator implements UniqueTicketIdGenerator {
    private final int tokenLength;

    @Override
    public String getNewTicketId(final String prefix) {
        return prefix + SEPARATOR + RandomUtils.randomNumeric(this.tokenLength);
    }
}
