package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationIdentityProviderFinalizeLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
class DelegatedAuthenticationIdentityProviderFinalizeLogoutActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_FINALIZE_LOGOUT)
    private Action action;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "CasClient");
        context.setParameter(casProperties.getLogout().getRedirectParameter().getFirst(), "https://github.com");
        context.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        assertNull(action.execute(context));
        assertNotNull(WebUtils.getLogoutRedirectUrl(context.getHttpServletRequest(), String.class));
    }
}
