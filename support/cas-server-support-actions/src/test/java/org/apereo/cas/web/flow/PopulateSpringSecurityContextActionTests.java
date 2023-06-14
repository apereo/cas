package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PopulateSpringSecurityContextActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowActions")
@Import(PopulateSpringSecurityContextActionTests.PopulateSpringSecurityContextActionTestConfiguration.class)
class PopulateSpringSecurityContextActionTests extends AbstractWebflowActionsTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_POPULATE_SECURITY_CONTEXT)
    private Action populateSpringSecurityContextAction;

    @Autowired
    @Qualifier("securityContextRepository")
    private SecurityContextRepository securityContextRepository;

    @Test
    void verifyOperation() throws Exception {
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val result = populateSpringSecurityContextAction.execute(context);
        assertNull(result);
        val sec = (SecurityContext) request.getSession()
            .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull(sec);
        assertNotNull(sec.getAuthentication());
        assertTrue(securityContextRepository.containsContext(request));
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class PopulateSpringSecurityContextActionTestConfiguration {
        @Bean
        public SecurityContextRepository securityContextRepository() {
            return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
            );
        }
    }
}
