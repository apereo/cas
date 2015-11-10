package org.jasig.cas.support.saml.authentication.principal;

import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertNull;

/**
 * Test cases for {@link GoogleAccountsServiceFactory}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceFactoryTests extends AbstractOpenSamlTests {
    @Autowired
    private GoogleAccountsServiceFactory factory;

    @Test
    public void verifyNoService() {
        assertNull(factory.createService(new MockHttpServletRequest()));
    }
}
