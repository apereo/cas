package org.apereo.cas.otp.web.flow;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepositoryTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OneTimeTokenAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowEvents")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseOneTimeTokenRepositoryTests.SharedTestConfiguration.class)
class OneTimeTokenAuthenticationWebflowEventResolverTests {
    @Autowired
    @Qualifier("oneTimeTokenAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver resolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyResolver() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val authn = RegisteredServiceTestUtils.getAuthentication("casuser");
        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authn));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authn, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resolver.resolveSingle(context).getId());
    }
}
