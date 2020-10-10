package org.apereo.cas.support.oauth.services;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20ServicesManagerRegisteredServiceLocatorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator oauthServicesManagerRegisteredServiceLocator;

    @Test
    public void verifyOperation() {
        assertNotNull(oauthServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, oauthServicesManagerRegisteredServiceLocator.getOrder());
        val service = getRegisteredService("clientid123456", UUID.randomUUID().toString());
        service.setMatchingStrategy(new PartialRegexRegisteredServiceMatchingStrategy());
        val svc = serviceFactory.createService(
            String.format("https://oauth.example.org/whatever?%s=clientid", OAuth20Constants.CLIENT_ID));
        val result = oauthServicesManagerRegisteredServiceLocator.locate(List.of(service),
            svc, r -> r.matches("https://oauth.example.org"));
        assertNotNull(result);
    }

}
