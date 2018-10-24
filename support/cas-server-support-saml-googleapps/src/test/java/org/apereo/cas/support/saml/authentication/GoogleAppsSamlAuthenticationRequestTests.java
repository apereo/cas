package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.config.SamlGoogleAppsConfiguration;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link GoogleAppsSamlAuthenticationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Import(SamlGoogleAppsConfiguration.class)
@TestPropertySource(locations = "classpath:/gapps.properties")
public class GoogleAppsSamlAuthenticationRequestTests extends AbstractOpenSamlTests {
    @Autowired
    private ApplicationContextProvider applicationContextProvider;

    @BeforeEach
    public void init() {
        this.applicationContextProvider.setApplicationContext(this.applicationContext);
    }

    @Test
    public void ensureInflation() {
        val deflator = CompressionUtils.deflate(SAML_REQUEST);
        val builder = new GoogleSaml20ObjectBuilder(configBean);
        val msg = builder.decodeSamlAuthnRequest(deflator);
        assertEquals(SAML_REQUEST, msg);
    }

}
