package org.apereo.cas.adaptors.duo.web.flow.action;

import module java.base;
import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityDirectAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    DuoSecurityDirectAuthenticationActionTests.DuoMultifactorTestConfiguration.class,
    BaseDuoSecurityTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=Q2IU2i8BFNd6VYflZT8Evl6lF7oPlj3PM15BmRU7",
        "cas.authn.mfa.duo[0].duo-integration-key=DIOXVRZD2UMZ8XXMNFQ5",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
class DuoSecurityDirectAuthenticationActionTests extends BaseCasWebflowMultifactorAuthenticationTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DUO_NON_WEB_AUTHENTICATION)
    private Action duoNonWebAuthenticationAction;

    @Test
    void verifyOperation() throws Throwable {
        val context = BaseDuoSecurityTests.getMockRequestContext(applicationContext);
        val provider = BaseDuoSecurityTests.getDuoSecurityMultifactorAuthenticationProvider();
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, provider);
        val event = duoNonWebAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @TestConfiguration(value = "DuoMultifactorTestConfiguration", proxyBeanMethods = false)
    static class DuoMultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider duoProvider() {
            return BaseDuoSecurityTests.getDuoSecurityMultifactorAuthenticationProvider();
        }
    }
}

