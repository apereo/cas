package org.apereo.cas.mfa.simple;

import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;

import java.util.EnumSet;

/**
 * This is {@link CasSimpleMultifactorTokenCommunicationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface CasSimpleMultifactorTokenCommunicationStrategy {

    /**
     * All strategy options allowed.
     *
     * @return the cas simple multifactor token communication strategy
     */
    static CasSimpleMultifactorTokenCommunicationStrategy all() {
        return token -> TokenSharingStrategyOptions.ALL;
    }

    /**
     * Determine strategy enum set.
     *
     * @param token the token
     * @return the enum set
     */
    EnumSet<TokenSharingStrategyOptions> determineStrategy(CasSimpleMultifactorAuthenticationTicket token);


    /**
     * The token sharing strategy options.
     */
    enum TokenSharingStrategyOptions {
        /**
         * Sms strategy option.
         */
        SMS,
        /**
         * Email strategy option.
         */
        EMAIL,
        /**
         * Notification strategy option.
         */
        NOTIFICATION;

        /**
         * All options.
         */
        public static final EnumSet<TokenSharingStrategyOptions> ALL = EnumSet.allOf(TokenSharingStrategyOptions.class);
    }
}
