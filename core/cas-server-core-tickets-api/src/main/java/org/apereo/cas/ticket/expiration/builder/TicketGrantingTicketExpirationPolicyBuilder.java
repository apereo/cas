package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link TicketGrantingTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Slf4j
@Getter
public class TicketGrantingTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<TicketGrantingTicket> {
    private static final long serialVersionUID = -4197980180617072826L;
    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        val tgt = casProperties.getTicket().getTgt();
        if (tgt.getRememberMe().isEnabled()) {
            val p = toRememberMeTicketExpirationPolicy();
            LOGGER.debug("Final effective time-to-live of remember-me expiration policy is [{}] seconds", p.getTimeToLive());
            return p;
        }
        val p = toTicketGrantingTicketExpirationPolicy();
        LOGGER.debug("Final effective time-to-live of ticket-granting ticket expiration policy is [{}] seconds", p.getTimeToLive());
        return p;
    }

    @Override
    public Class<TicketGrantingTicket> getTicketType() {
        return TicketGrantingTicket.class;
    }

    /**
     * To ticket-granting ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toTicketGrantingTicketExpirationPolicy() {
        val tgt = casProperties.getTicket().getTgt();
        if (tgt.getMaxTimeToLiveInSeconds() <= 0 && tgt.getTimeToKillInSeconds() <= 0) {
            LOGGER.warn("Ticket-granting ticket expiration policy is set to NEVER expire tickets.");
            return NeverExpiresExpirationPolicy.INSTANCE;
        }

        if (tgt.getTimeout().getMaxTimeToLiveInSeconds() > 0) {
            LOGGER.debug("Ticket-granting ticket expiration policy is based on a timeout of [{}] seconds",
                tgt.getTimeout().getMaxTimeToLiveInSeconds());
            return new TimeoutExpirationPolicy(tgt.getTimeout().getMaxTimeToLiveInSeconds());
        }

        if (tgt.getThrottledTimeout().getTimeInBetweenUsesInSeconds() > 0
            && tgt.getThrottledTimeout().getTimeToKillInSeconds() > 0) {
            val p = new ThrottledUseAndTimeoutExpirationPolicy();
            p.setTimeToKillInSeconds(tgt.getThrottledTimeout().getTimeToKillInSeconds());
            p.setTimeInBetweenUsesInSeconds(tgt.getThrottledTimeout().getTimeInBetweenUsesInSeconds());
            LOGGER.debug("Ticket-granting ticket expiration policy is based on throttled timeouts");
            return p;
        }

        if (tgt.getHardTimeout().getTimeToKillInSeconds() > 0) {
            LOGGER.debug("Ticket-granting ticket expiration policy is based on a hard timeout of [{}] seconds",
                tgt.getHardTimeout().getTimeToKillInSeconds());
            return new HardTimeoutExpirationPolicy(tgt.getHardTimeout().getTimeToKillInSeconds());
        }

        if (tgt.getMaxTimeToLiveInSeconds() > 0 && tgt.getTimeToKillInSeconds() > 0) {
            LOGGER.debug("Ticket-granting ticket expiration policy is based on hard/idle timeouts of [{}]/[{}] seconds",
                tgt.getMaxTimeToLiveInSeconds(), tgt.getTimeToKillInSeconds());
            return new TicketGrantingTicketExpirationPolicy(tgt.getMaxTimeToLiveInSeconds(), tgt.getTimeToKillInSeconds());
        }
        LOGGER.warn("Ticket-granting ticket expiration policy is set to ALWAYS expire tickets.");
        return new AlwaysExpiresExpirationPolicy();
    }

    /**
     * To remember-me ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toRememberMeTicketExpirationPolicy() {
        val tgt = casProperties.getTicket().getTgt();
        LOGGER.debug("Remember me expiration policy is being configured based on hard timeout of [{}] seconds",
            tgt.getRememberMe().getTimeToKillInSeconds());
        val rememberMePolicy = new HardTimeoutExpirationPolicy(tgt.getRememberMe().getTimeToKillInSeconds());
        val p = new RememberMeDelegatingExpirationPolicy();
        p.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_REMEMBER_ME, rememberMePolicy);
        p.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_DEFAULT, toTicketGrantingTicketExpirationPolicy());
        return p;
    }
}
