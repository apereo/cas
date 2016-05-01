package org.jasig.cas.support.saml.authentication;

import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.jasig.cas.util.ApplicationContextProvider;
import org.jasig.cas.util.CompressionUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * This is {@link GoogleAppsSamlAuthenticationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class GoogleAppsSamlAuthenticationRequestTests extends AbstractOpenSamlTests {

    @Autowired
    private ApplicationContextProvider applicationContextProvider;

    @Before
    public void init() {
        this.applicationContextProvider.setApplicationContext(this.applicationContext);
    }

    @Test
    public void ensureInflation() throws Exception {
        final String deflator = CompressionUtils.deflate(SAML_REQUEST);
        final AbstractSaml20ObjectBuilder builder = new GoogleSaml20ObjectBuilder();
        final String msg = builder.decodeSamlAuthnRequest(deflator);
        assertEquals(msg, SAML_REQUEST);
    }

}
