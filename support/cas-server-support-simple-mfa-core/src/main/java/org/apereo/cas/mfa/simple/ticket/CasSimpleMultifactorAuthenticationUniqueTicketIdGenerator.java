package org.apereo.cas.mfa.simple.ticket;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.simple.CoreCasSimpleMultifactorAuthenticationTokenProperties.TokenType;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator implements UniqueTicketIdGenerator {
    private final CasConfigurationProperties casProperties;

    @Override
    public String getNewTicketId(final String prefix) {
        val properties = casProperties.getAuthn().getMfa().getSimple().getToken().getCore();
        val code = switch (properties.getTokenType()) {
            case TokenType.ALPHABETIC -> RandomUtils.randomAlphabetic(properties.getTokenLength());
            case TokenType.ALPHANUMERIC -> RandomUtils.randomAlphanumeric(properties.getTokenLength());
            case TokenType.NUMERIC -> RandomUtils.randomNumeric(properties.getTokenLength());
        };
        return prefix + SEPARATOR + code;
    }
}
