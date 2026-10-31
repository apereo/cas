package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasTokenAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasTokenAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.token.authentication.TokenAuthenticationSecurity;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TokenAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    BaseCasCoreTests.SharedTestConfiguration.class,
    CasTokenAuthenticationAutoConfiguration.class,
    CasTokenAuthenticationWebflowAutoConfiguration.class
})
@Tag("WebflowAuthenticationActions")
@ExtendWith(CasTestExtension.class)
class TokenAuthenticationActionTests extends AbstractCentralAuthenticationServiceTests {
    private static final RandomStringGenerator RANDOM_STRING_GENERATOR = new DefaultRandomStringGenerator();

    private static final String SIGNING_SECRET = RANDOM_STRING_GENERATOR.getNewString(256);

    private static final String ENCRYPTION_SECRET = RANDOM_STRING_GENERATOR.getNewString(48);

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_TOKEN_AUTHENTICATION_ACTION)
    private Action action;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @BeforeEach
    void before() {
        val svc = RegisteredServiceTestUtils.getRegisteredService("https://example.token.org");
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        val prop = new DefaultRegisteredServiceProperty();
        prop.addValue(SIGNING_SECRET);
        svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName(), prop);

        val prop2 = new DefaultRegisteredServiceProperty();
        prop2.addValue(ENCRYPTION_SECRET);
        svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION.getPropertyName(), prop2);
        servicesManager.save(svc);
    }

    @Test
    void verifyAction() throws Throwable {
        val service = RegisteredServiceTestUtils.getService("https://example.token.org");
        val authentication = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser", Map.of(
            "uid", List.of("uid"), "givenName", List.of("CASUser"), "memberOf", List.of("system", "cas", "admin"))));
        val token = TokenAuthenticationSecurity.forRegisteredService(servicesManager.findServiceBy(service))
            .generateTokenFor(authentication)
            .orElseThrow();

        val context = MockRequestContext.create(applicationContext);
        context.addHeader(TokenConstants.PARAMETER_NAME_TOKEN, token);

        WebUtils.putServiceIntoFlowScope(context, service);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }
}
