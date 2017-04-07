package org.apereo.cas.logout;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import java.net.URL;

/**
 * This is {@link DefaultSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(JUnit4.class)
public class DefaultSingleLogoutServiceLogoutUrlBuilderTests {

    @Test
    public void verifyLogoutUrlByService() throws Exception {
        final AbstractRegisteredService svc = RegisteredServiceTestUtils.getRegisteredService("https://www.google.com");
        svc.setLogoutUrl(new URL("http://www.example.com/logout"));
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = new DefaultSingleLogoutServiceLogoutUrlBuilder();
        final URL url = builder.determineLogoutUrl(svc, RegisteredServiceTestUtils.getService("https://www.google.com"));
        assertEquals(url, svc.getLogoutUrl());
    }

    @Test
    public void verifyLogoutUrlByDefault() throws Exception {
        final AbstractRegisteredService svc = RegisteredServiceTestUtils.getRegisteredService(".+");
        svc.setLogoutUrl(null);
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = new DefaultSingleLogoutServiceLogoutUrlBuilder();
        final URL url = builder.determineLogoutUrl(svc, RegisteredServiceTestUtils.getService("https://www.somewhere.com/logout?p=v"));
        assertEquals(url, new URL("https://www.somewhere.com/logout?p=v"));
    }

    @Test
    public void verifyLogoutUrlUnknownUrlProtocol() throws Exception {
        final AbstractRegisteredService svc = RegisteredServiceTestUtils.getRegisteredService(".+");
        svc.setLogoutUrl(null);
        final DefaultSingleLogoutServiceLogoutUrlBuilder builder = new DefaultSingleLogoutServiceLogoutUrlBuilder();
        final URL url = builder.determineLogoutUrl(svc, RegisteredServiceTestUtils.getService("imaps://etc.example.org"));
        assertNull(url);       
    }
}
