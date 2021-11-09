package org.apereo.cas.okta;

import org.apereo.cas.config.OktaConfigurationFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;

import com.okta.sdk.impl.oauth2.OAuth2TokenRetrieverException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OktaConfigurationFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Authentication")
@SpringBootTest(classes = BaseOktaTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.okta.proxy-host=localhost",
        "cas.authn.okta.proxy-port=8516",
        "cas.authn.okta.organization-url=https://dev-159539.oktapreview.com",

        "cas.authn.attribute-repository.okta.organization-url=https://dev-668371.oktapreview.com",
        "cas.authn.attribute-repository.okta.api-token=0030j4HfPHEIQG39pl0nNacnx2bqqZMqDq6Hk5wfNa",
        "cas.authn.attribute-repository.okta.proxy-host=localhost",
        "cas.authn.attribute-repository.okta.proxy-port=8923",
        "cas.authn.attribute-repository.okta.proxy-username=user",
        "cas.authn.attribute-repository.okta.proxy-password=pass",
        "cas.authn.attribute-repository.okta.client-id=dummy-client",
        "cas.authn.attribute-repository.okta.private-key.location=classpath:okta-private-key.pem"
    })
public class OktaConfigurationFactoryTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() {
        try (val webServer = new MockWebServer(8923, HttpStatus.OK)) {
            webServer.start();
            assertNotNull(OktaConfigurationFactory.buildAuthenticationClient(casProperties.getAuthn().getOkta()));
            assertThrows(OAuth2TokenRetrieverException.class,
                () -> OktaConfigurationFactory.buildClient(casProperties.getAuthn().getAttributeRepository().getOkta()));
        }

    }
}
