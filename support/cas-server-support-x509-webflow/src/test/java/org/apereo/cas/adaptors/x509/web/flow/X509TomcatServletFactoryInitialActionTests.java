package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.X509TomcatServletFactoryInitialAction;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509TomcatServletFactoryInitialActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreUtilAutoConfiguration.class, properties = "cas.authn.x509.webflow.port=9876")
class X509TomcatServletFactoryInitialActionTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() throws Throwable {
        val action = new X509TomcatServletFactoryInitialAction(casProperties);

        val context = MockRequestContext.create(applicationContext);

        val result = action.execute(context);
        assertNull(result);

        assertNotNull(context.getFlowScope().get(X509TomcatServletFactoryInitialAction.ATTRIBUTE_X509_CLIENT_AUTH_LOGIN_ENDPOINT_URL));
    }

}
