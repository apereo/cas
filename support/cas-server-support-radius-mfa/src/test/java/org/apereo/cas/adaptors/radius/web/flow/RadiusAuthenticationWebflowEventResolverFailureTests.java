package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenCredential;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RadiusAuthenticationWebflowEventResolverFailureTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseRadiusMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.radius.client.shared-secret=NoSecret",
        "cas.authn.radius.client.inet-address=localhost,localguest",
        "cas.authn.mfa.radius.allowed-authentication-attempts=2",
        "cas.authn.mfa.radius.client.shared-secret=NoSecret",
        "cas.authn.mfa.radius.client.inet-address=localhost,localguest"
    })
@Tag("Radius")
@ExtendWith(CasTestExtension.class)
class RadiusAuthenticationWebflowEventResolverFailureTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("radiusAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver radiusAuthenticationWebflowEventResolver;

    private RequestContext context;

    @BeforeEach
    void initialize() throws Exception {
        this.context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        WebUtils.putAuthentication(authentication, context);
        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authentication));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);
        WebUtils.putAuthenticationResultBuilder(builder, context);
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        WebUtils.putCredential(context, new RadiusTokenCredential("token"));
        var event = radiusAuthenticationWebflowEventResolver.resolveSingle(this.context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertTrue(context.getFlowScope().contains(RadiusAuthenticationWebflowEventResolver.FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS));
        event = radiusAuthenticationWebflowEventResolver.resolveSingle(this.context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_CANCEL, event.getId());
        assertFalse(context.getFlowScope().contains(RadiusAuthenticationWebflowEventResolver.FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS));
    }

}
