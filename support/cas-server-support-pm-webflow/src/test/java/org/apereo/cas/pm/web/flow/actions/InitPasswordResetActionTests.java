package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InitPasswordResetActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Mail")
class InitPasswordResetActionTests extends BasePasswordManagementActionTests {

    @Nested
    class DefaultTests extends BasePasswordManagementActionTests {
        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val request = context.getHttpServletRequest();
            request.setRemoteAddr("1.2.3.4");
            request.setLocalAddr("1.2.3.4");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));

            val query = PasswordManagementQuery.builder().username(UUID.randomUUID().toString()).build();
            val token = passwordManagementService.createToken(query);

            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, initPasswordResetAction.execute(context).getId());

            context.getFlowScope().put("token", token);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, initPasswordResetAction.execute(context).getId());
            val credential = WebUtils.getCredential(context, UsernamePasswordCredential.class);
            assertNotNull(credential);
            assertEquals(query.getUsername(), credential.getUsername());
        }

        @Test
        void verifyActionUserlessToken() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().build());
            context.getFlowScope().put("token", token);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, initPasswordResetAction.execute(context).getId());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.pm.reset.multifactor-authentication-enabled=true")
    @Import(CasSimpleMultifactorAuthenticationAutoConfiguration.class)
    class MfaRequiredTests extends BasePasswordManagementActionTests {
        @Autowired
        @Qualifier("casSimpleMultifactorAuthenticationProvider")
        private MultifactorAuthenticationProvider multifactorAuthenticationProvider;

        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val request = context.getHttpServletRequest();
            request.setRemoteAddr("1.2.3.4");
            request.setLocalAddr("1.2.3.4");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));

            val query = PasswordManagementQuery.builder().username(UUID.randomUUID().toString()).build();
            val token = passwordManagementService.createToken(query);
            context.getFlowScope().put("token", token);
            val event = initPasswordResetAction.execute(context);
            assertEquals(multifactorAuthenticationProvider.getId(), event.getId());
            assertNotNull(WebUtils.getAuthentication(context));
            assertNotNull(WebUtils.getAuthenticationResultBuilder(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_RESUME_RESET_PASSWORD, WebUtils.getTargetTransition(context));
            assertEquals(multifactorAuthenticationProvider.getId(), WebUtils.getMultifactorAuthenticationProvider(context));
        }
    }
}
