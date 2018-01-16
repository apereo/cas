package org.apereo.cas.support.saml.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.config.SamlGoogleAppsConfiguration;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link GoogleAppsSamlAuthenticationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(SpringRunner.class)
@Import(SamlGoogleAppsConfiguration.class)
@TestPropertySource(locations = "classpath:/gapps.properties")
@Slf4j
public class GoogleAppsSamlAuthenticationRequestTests extends AbstractOpenSamlTests {

    @Autowired
    private ApplicationContextProvider applicationContextProvider;

    @Before
    public void init() {
        this.applicationContextProvider.setApplicationContext(this.applicationContext);
    }

    @Test
    public void ensureInflation() {
        final String deflator = CompressionUtils.deflate(SAML_REQUEST);
        final GoogleSaml20ObjectBuilder builder = new GoogleSaml20ObjectBuilder(configBean);
        final String msg = builder.decodeSamlAuthnRequest(deflator);
        assertEquals(SAML_REQUEST, msg);
    }

}
