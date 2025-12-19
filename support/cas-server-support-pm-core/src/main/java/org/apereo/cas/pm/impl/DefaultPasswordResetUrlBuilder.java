package org.apereo.cas.pm.impl;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriUtils;

/**
 * This is {@link DefaultPasswordResetUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultPasswordResetUrlBuilder implements PasswordResetUrlBuilder {
    protected final PasswordManagementService passwordManagementService;

    protected final TicketRegistry ticketRegistry;

    protected final TicketFactory ticketFactory;

    protected final CasConfigurationProperties casProperties;

    @Override
    public URL build(final String username, final WebApplicationService service) throws Throwable {
        val query = PasswordManagementQuery.builder().username(username).build();
        LOGGER.debug("Creating password reset URL designed for [{}]", username);

        val token = passwordManagementService.createToken(query);
        if (StringUtils.isNotBlank(token)) {
            val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);

            val properties = CollectionUtils.<String, Serializable>wrap(
                PasswordManagementService.PARAMETER_TOKEN, token,
                ExpirationPolicy.class.getName(), computeExpirationPolicy());
            val ticket = transientFactory.create(service, properties);
            ticketRegistry.addTicket(ticket);

            val resetUrl = new StringBuilder(casProperties.getServer().getPrefix())
                .append('/').append(CasWebflowConfigurer.FLOW_ID_LOGIN).append('?')
                .append(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN).append('=').append(ticket.getId());

            if (service != null) {
                val encodeServiceUrl = UriUtils.encode(service.getOriginalUrl(), StandardCharsets.UTF_8);
                resetUrl.append('&').append(CasProtocolConstants.PARAMETER_SERVICE).append('=').append(encodeServiceUrl);
            }

            val url = resetUrl.toString();
            LOGGER.debug("Final password reset URL designed for [{}] is [{}]", username, url);
            return new URI(url).toURL();
        }
        LOGGER.error("Could not create password reset url since no reset token could be generated");
        return null;
    }

    protected ExpirationPolicy computeExpirationPolicy() {
        val reset = casProperties.getAuthn().getPm().getReset();
        val numberOfUses = reset.getNumberOfUses();
        val seconds = Beans.newDuration(reset.getExpiration()).toSeconds();

        if (numberOfUses >= 1) {
            LOGGER.debug("Password reset URL shall expire after [{}] uses and in [{}] second(s)", numberOfUses, seconds);
            return new MultiTimeUseOrTimeoutExpirationPolicy(numberOfUses, seconds);
        }

        LOGGER.debug("Password reset URL shall expire in [{}] second(s)", seconds);
        return HardTimeoutExpirationPolicy.builder().timeToKillInSeconds(seconds).build();
    }
}
