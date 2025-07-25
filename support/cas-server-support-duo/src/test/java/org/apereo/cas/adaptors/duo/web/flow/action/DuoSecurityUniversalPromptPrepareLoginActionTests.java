package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityUniversalPromptPrepareLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
@SuppressWarnings("EffectivelyPrivate")
class DuoSecurityUniversalPromptPrepareLoginActionTests {

    @SpringBootTest(classes = BaseDuoSecurityTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.mfa.duo[0].duo-secret-key=Q2IU2i8BFNd6VYflZT8Evl6lF7oPlj3PM15BmRU7",
            "cas.authn.mfa.duo[0].duo-integration-key=DIOXVRZD2UMZ8XXMNFQ5",
            "cas.authn.mfa.duo[0].trusted-device-enabled=true",
            "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
        })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    private abstract static class BaseDuoSecurityUniversalPromptTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_PREPARE_LOGIN)
        protected Action duoUniversalPromptPrepareLoginAction;
    }

    @Nested
    class DefaultTests extends BaseDuoSecurityUniversalPromptTests {
        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val authentication = RegisteredServiceTestUtils.getAuthentication();
            WebUtils.putAuthentication(authentication, context);
            WebUtils.putAuthenticationResult(RegisteredServiceTestUtils.getAuthenticationResult(authentication.getPrincipal().getId()), context);
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
            val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(
                DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER, applicationContext).orElseThrow();
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, provider);
            val result = duoUniversalPromptPrepareLoginAction.execute(context);
            assertNotNull(result);
            assertNotNull(result.getAttributes().get("result"));
            assertTrue(context.getFlowScope().contains("duoUniversalPromptLoginUrl"));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.duo[0].session-storage-type=TICKET_REGISTRY")
    class TicketRegistryStorageTests extends BaseDuoSecurityUniversalPromptTests {
        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val authentication = RegisteredServiceTestUtils.getAuthentication();
            WebUtils.putAuthentication(authentication, context);
            WebUtils.putAuthenticationResult(RegisteredServiceTestUtils.getAuthenticationResult(authentication.getPrincipal().getId()), context);
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
            val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(
                DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER, applicationContext).orElseThrow();
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, provider);
            val result = duoUniversalPromptPrepareLoginAction.execute(context);
            assertNotNull(result);
            assertNotNull(result.getAttributes().get("result"));
            assertTrue(context.getFlowScope().contains("duoUniversalPromptLoginUrl"));
        }
    }
    
    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.duo[0].principal-attribute=email")
    class PrincipalAttributeTests extends BaseDuoSecurityUniversalPromptTests {
        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val authentication = RegisteredServiceTestUtils.getAuthentication(
                RegisteredServiceTestUtils.getPrincipal(Map.of("email", List.of("casuser@example.org"))));
            WebUtils.putAuthentication(authentication, context);
            WebUtils.putAuthenticationResult(RegisteredServiceTestUtils.getAuthenticationResult(authentication.getPrincipal().getId()), context);
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
            val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(
                DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER, applicationContext).orElseThrow();
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, provider);
            val result = duoUniversalPromptPrepareLoginAction.execute(context);
            assertNotNull(result);
            val sessionStorage = (BrowserStorage) result.getAttributes().get("result");
            assertNotNull(sessionStorage);

            val request = new URIBuilder(sessionStorage.getDestinationUrl()).getFirstQueryParam("request").getValue();
            val duoUsername = SignedJWT.parse(request).getJWTClaimsSet().getStringClaim("duo_uname");
            assertEquals("casuser@example.org", duoUsername);
            assertTrue(context.getFlowScope().contains("duoUniversalPromptLoginUrl"));
        }
    }
    
}
