package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
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
import org.springframework.webflow.execution.Action;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LoadSurrogatesListActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
@SpringBootTest(classes = BaseSurrogateAuthenticationTests.SharedTestConfiguration.class, properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@ExtendWith(CasTestExtension.class)
class LoadSurrogatesListActionTests extends BaseSurrogateAuthenticationTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_LOAD_SURROGATES_LIST_ACTION)
    protected Action loadSurrogatesListAction;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Test
    void verifyGetListView() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        WebUtils.putSurrogateAuthenticationRequest(context, true);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

        assertEquals(CasWebflowConstants.TRANSITION_ID_SURROGATE_VIEW, loadSurrogatesListAction.execute(context).getId());
        assertNotNull(WebUtils.getSurrogateAuthenticationAccounts(context));
    }

    @Test
    void verifyAuthenticate() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val attributes = new LinkedHashMap<String, List<Object>>();
        attributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, List.of(true));
        attributes.putAll(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal), context);

        val creds = new UsernamePasswordCredential();
        creds.assignPassword("Mellon");
        creds.setUsername("casuser");
        creds.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        WebUtils.putCredential(context, creds);

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(CoreAuthenticationTestUtils.getAuthentication("casuser")));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, loadSurrogatesListAction.execute(context).getId());
    }

    @Test
    void verifyAuthenticateNotAuthorized() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val attributes = new LinkedHashMap<String, List<Object>>();
        attributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, List.of(true));
        attributes.putAll(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());

        val p = CoreAuthenticationTestUtils.getPrincipal("casuser", attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(p), context);

        val creds = new UsernamePasswordCredential();
        creds.assignPassword("Mellon");
        creds.setUsername("casuser");
        creds.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("unknown-user"));
        WebUtils.putCredential(context, creds);

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(CoreAuthenticationTestUtils.getAuthentication("casuser")));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, loadSurrogatesListAction.execute(context).getId());
    }

    @Test
    void verifySkipAuthenticate() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putSurrogateAuthenticationRequest(context, Boolean.TRUE);

        val attributes = new LinkedHashMap<String, List<Object>>();
        attributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, List.of(true));
        attributes.putAll(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());

        val p = CoreAuthenticationTestUtils.getPrincipal("someuser", attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(p), context);

        val creds = new UsernamePasswordCredential();
        creds.assignPassword("Mellon");
        creds.setUsername("someuser");
        creds.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("others"));
        WebUtils.putCredential(context, creds);

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(CoreAuthenticationTestUtils.getAuthentication("casuser")));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP_SURROGATE, loadSurrogatesListAction.execute(context).getId());
    }
}
