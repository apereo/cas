package org.apereo.cas.oidc.services;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;

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
 * This is {@link OidcServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcServicesManagerRegisteredServiceLocatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator oidcServicesManagerRegisteredServiceLocator;

    @Test
    public void verifyOperation() {
        assertNotNull(oidcServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, oidcServicesManagerRegisteredServiceLocator.getOrder());
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        service.setMatchingStrategy(new PartialRegexRegisteredServiceMatchingStrategy());
        val result = oidcServicesManagerRegisteredServiceLocator.locate(List.of(service),
            "https://oauth.example.org/whatever",
            r -> r.matches("https://oauth.example.org/whatever"));
        assertNotNull(result);
    }

}
