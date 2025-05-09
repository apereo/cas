package org.apereo.cas.multitenancy;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTenantExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Web")
@SpringBootTest(classes = {
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    BaseMultitenancyTests.SharedTestConfiguration.class
},
    properties = {
        "cas.sso.proxy-authn-enabled=false",
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@ExtendWith(CasTestExtension.class)
class DefaultTenantExtractorTests {

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
    private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;


    @Test
    void verifyOperation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        requestContext.setContextPath("/cas/tenants/b9584c42/login");
        requestContext.setClientInfo();

        val definition = tenantExtractor.extract(requestContext);
        assertTrue(definition.isPresent());
        assertFalse(definition.get().getAuthenticationPolicy().getAuthenticationHandlers().isEmpty());

        val handlers = authenticationEventExecutionPlan.resolveAuthenticationHandlers();
        assertEquals(1, handlers.size());
        assertEquals(SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName(), handlers.iterator().next().getName());
    }

    @Test
    void verifyBlank() {
        val definition = tenantExtractor.extract(StringUtils.EMPTY);
        assertFalse(definition.isPresent());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "tenants/b9584c42/login",
        "/tenants/b9584c42",
        "/cas/tenants/b9584c42/p3/validate",
        "/tenants/b9584c42/p3/validate",
        "/tenants/b9584c42/login",
        "/tenants/b9584c42/idp/profile/SAML2/POST/SLO"
    })
    void verifyTenantPathExtraction(final String path) {
        assertEquals("b9584c42", TenantExtractor.tenantIdFromPath(path));
    }
}
