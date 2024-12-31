package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockRequestContext;
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
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.webflow.execution.Action;
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
    void verifyOperation() throws Throwable {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val result = populateSpringSecurityContextAction.execute(context);
        assertNull(result);
        val sec = securityContextRepository.loadDeferredContext(context.getHttpServletRequest()).get();
        assertNotNull(sec);
        assertNotNull(sec.getAuthentication());
        assertTrue(securityContextRepository.containsContext(context.getHttpServletRequest()));
    }

    @TestConfiguration(value = "PopulateSpringSecurityContextActionTestConfiguration", proxyBeanMethods = false)
    static class PopulateSpringSecurityContextActionTestConfiguration {
        @Bean
        public SecurityContextRepository securityContextRepository() {
            return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
            );
        }
    }
}
