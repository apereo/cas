package org.apereo.cas.ticket.expiration.builder;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link TicketGrantingTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
public record TicketGrantingTicketExpirationPolicyBuilder(CasConfigurationProperties casProperties) implements ExpirationPolicyBuilder<TicketGrantingTicket> {
    @Serial
    private static final long serialVersionUID = -4197980180617072826L;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        val tgt = casProperties.getTicket().getTgt();
        if (tgt.getRememberMe().isEnabled()) {
            val policy = toRememberMeTicketExpirationPolicy();
            LOGGER.debug("Final effective time-to-live of remember-me expiration policy is [{}] seconds", policy.getTimeToLive());
            return policy;
        }
        val policy = toTicketGrantingTicketExpirationPolicy();
        LOGGER.debug("Final effective time-to-live of ticket-granting ticket expiration policy is [{}] seconds", policy.getTimeToLive());
        return policy;
    }

    /**
     * To remember-me ticket expiration policy.
     *
     * @return the expiration policy
     */
    public ExpirationPolicy toRememberMeTicketExpirationPolicy() {
        val tgt = casProperties.getTicket().getTgt();
        val timeToKillInSeconds = Beans.newDuration(tgt.getRememberMe().getTimeToKillInSeconds()).toSeconds();
        LOGGER.debug("Remember me expiration policy is being configured based on hard timeout of [{}] seconds", timeToKillInSeconds);
        val rememberMePolicy = new HardTimeoutExpirationPolicy(timeToKillInSeconds);
        val policy = new RememberMeDelegatingExpirationPolicy();
        policy.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_REMEMBER_ME, rememberMePolicy);
        policy.addPolicy(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT, toTicketGrantingTicketExpirationPolicy());
        return policy;
    }

    private ExpirationPolicy toTicketGrantingTicketExpirationPolicy() {
        val tgt = casProperties.getTicket().getTgt();

        if (Beans.isInfinitelyDurable(tgt.getPrimary().getMaxTimeToLiveInSeconds())
            && Beans.isInfinitelyDurable(tgt.getPrimary().getTimeToKillInSeconds())) {
            LOGGER.warn("Primary ticket-granting ticket expiration policy is set to NEVER expire tickets.");
            return NeverExpiresExpirationPolicy.INSTANCE;
        }
        if (Beans.isNeverDurable(tgt.getPrimary().getMaxTimeToLiveInSeconds())
            && Beans.isNeverDurable(tgt.getPrimary().getTimeToKillInSeconds())) {
            LOGGER.warn("Ticket-granting ticket expiration policy is set to ALWAYS expire tickets.");
            return AlwaysExpiresExpirationPolicy.INSTANCE;
        }

        if (StringUtils.isNotBlank(tgt.getTimeout().getMaxTimeToLiveInSeconds())) {
            val seconds = Beans.newDuration(tgt.getTimeout().getMaxTimeToLiveInSeconds()).toSeconds();
            LOGGER.debug("Ticket-granting ticket expiration policy is based on a timeout of [{}] seconds", seconds);
            return new TimeoutExpirationPolicy(seconds);
        }

        if (StringUtils.isNotBlank(tgt.getThrottledTimeout().getTimeInBetweenUsesInSeconds())
            && StringUtils.isNotBlank(tgt.getThrottledTimeout().getTimeToKillInSeconds())) {
            val policy = new ThrottledUseAndTimeoutExpirationPolicy();
            val seconds = Beans.newDuration(tgt.getThrottledTimeout().getTimeToKillInSeconds()).toSeconds();
            val timeInBetweenSeconds = Beans.newDuration(tgt.getThrottledTimeout().getTimeInBetweenUsesInSeconds()).toSeconds();
            policy.setTimeToKillInSeconds(seconds);
            policy.setTimeInBetweenUsesInSeconds(timeInBetweenSeconds);
            LOGGER.debug("Ticket-granting ticket expiration policy is based on throttled timeouts");
            return policy;
        }

        if (StringUtils.isNotBlank(tgt.getHardTimeout().getTimeToKillInSeconds())) {
            val seconds = Beans.newDuration(tgt.getHardTimeout().getTimeToKillInSeconds()).toSeconds();
            LOGGER.debug("Ticket-granting ticket expiration policy is based on a hard timeout of [{}] seconds", seconds);
            return new HardTimeoutExpirationPolicy(seconds);
        }

        val maxTimePrimarySeconds = Beans.newDuration(tgt.getPrimary().getMaxTimeToLiveInSeconds()).toSeconds();
        val ttlPrimarySeconds = Beans.newDuration(tgt.getPrimary().getTimeToKillInSeconds()).toSeconds();
        LOGGER.debug("Ticket-granting ticket expiration policy is based on hard/idle timeouts of [{}]/[{}] seconds", maxTimePrimarySeconds, ttlPrimarySeconds);
        return new TicketGrantingTicketExpirationPolicy(maxTimePrimarySeconds, ttlPrimarySeconds);
    }
}
