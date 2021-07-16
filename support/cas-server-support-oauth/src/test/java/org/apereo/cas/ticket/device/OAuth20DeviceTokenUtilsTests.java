package org.apereo.cas.ticket.device;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthDeviceTokenExpirationPolicy;
import org.apereo.cas.ticket.expiration.builder.TicketGrantingTicketExpirationPolicyBuilder;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DeviceTokenUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20DeviceTokenUtilsTests extends AbstractOAuth20Tests {

    @BeforeEach
    public void setup() {
        super.setup();
        this.servicesManager.deleteAll();
    }

    @Test
    public void verifyDefault() {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = getRegisteredService(service.getId(), UUID.randomUUID().toString(), CLIENT_SECRET);
        registeredService.setDeviceTokenExpirationPolicy(null);
        servicesManager.save(registeredService);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(casProperties);
        val policy = OAuth20DeviceTokenUtils.determineExpirationPolicyForService(servicesManager, builder, service);
        assertEquals(28800, policy.getTimeToLive());
    }

    @Test
    public void verifyCustom() {
        val service = RegisteredServiceTestUtils.getService();
        val registeredService = getRegisteredService(service.getId(), UUID.randomUUID().toString(), CLIENT_SECRET);
        service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(registeredService.getClientId()));
        
        registeredService.setDeviceTokenExpirationPolicy(new DefaultRegisteredServiceOAuthDeviceTokenExpirationPolicy("PT60S"));
        servicesManager.save(registeredService);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(casProperties);
        val policy = OAuth20DeviceTokenUtils.determineExpirationPolicyForService(servicesManager, builder, service);
        assertEquals(60, policy.getTimeToLive());
    }

}
