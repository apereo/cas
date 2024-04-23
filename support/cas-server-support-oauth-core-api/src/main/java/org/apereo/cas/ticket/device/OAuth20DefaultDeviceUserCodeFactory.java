package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * Default OAuth device token factory.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class OAuth20DefaultDeviceUserCodeFactory implements OAuth20DeviceUserCodeFactory {
    protected final UniqueTicketIdGenerator deviceTokenIdGenerator;

    @Getter
    protected final ExpirationPolicyBuilder<OAuth20DeviceToken> expirationPolicyBuilder;

    protected final int userCodeLength;

    protected final ServicesManager servicesManager;

    @Override
    public OAuth20DeviceUserCode createDeviceUserCode(final String id, final Service service) {
        val userCode = StringUtils.defaultIfBlank(id, normalizeUserCode(RandomUtils.randomAlphanumeric(userCodeLength)));
        val expirationPolicyToUse = OAuth20DeviceTokenUtils.determineExpirationPolicyForService(servicesManager, expirationPolicyBuilder, service);
        return new OAuth20DefaultDeviceUserCode(normalizeUserCode(userCode), service, expirationPolicyToUse);
    }

    @Override
    public String normalizeUserCode(final String providedCode) {
        return StringUtils.prependIfMissingIgnoreCase(providedCode, OAuth20DeviceUserCode.PREFIX + '-');
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OAuth20DeviceUserCode.class;
    }

}
