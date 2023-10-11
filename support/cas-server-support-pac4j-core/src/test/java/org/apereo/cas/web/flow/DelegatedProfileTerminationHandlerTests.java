package org.apereo.cas.web.flow;

import org.apereo.cas.config.Pac4jCoreConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.SessionTerminationHandler;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedProfileTerminationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    Pac4jCoreConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Delegation")
public class DelegatedProfileTerminationHandlerTests {
    @Autowired
    @Qualifier("delegatedProfileTerminationHandler")
    private SessionTerminationHandler delegatedProfileTerminationHandler;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, "https://github.com");
        val results = delegatedProfileTerminationHandler.beforeSessionTermination(context);
        assertFalse(results.isEmpty());
        delegatedProfileTerminationHandler.afterSessionTermination(results, context);
        assertNotNull(context.getSessionAttribute(Pac4jConstants.REQUESTED_URL));
    }
}
