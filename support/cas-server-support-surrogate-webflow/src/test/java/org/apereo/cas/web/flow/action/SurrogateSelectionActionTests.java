package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateSelectionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
@SpringBootTest(classes = BaseSurrogateAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@ExtendWith(CasTestExtension.class)
class SurrogateSelectionActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SELECT_SURROGATE_ACTION)
    private Action selectSurrogateAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyFails() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setExternalContext(mock(ServletExternalContext.class));
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, selectSurrogateAction.execute(context).getId());
    }

    @Test
    void verifyNoCredentialFound() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(SurrogateSelectionAction.PARAMETER_NAME_SURROGATE_TARGET, "cassurrogate");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, selectSurrogateAction.execute(context).getId());
        val credential = WebUtils.getCredential(context);
        assertNull(credential);
    }

    @Test
    void verifyCredentialFound() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(CoreAuthenticationTestUtils.getAuthentication("casuser")));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        context.setParameter(SurrogateSelectionAction.PARAMETER_NAME_SURROGATE_TARGET, "cassurrogate");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, selectSurrogateAction.execute(context).getId());
    }
}
