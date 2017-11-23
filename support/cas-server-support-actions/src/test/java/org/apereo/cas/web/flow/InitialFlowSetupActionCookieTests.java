package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link InitialFlowSetupActionCookieTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = "spring.aop.proxy-target-class=true")
@Import(CasSupportActionsConfiguration.class)
public class InitialFlowSetupActionCookieTests extends AbstractCentralAuthenticationServiceTests {

    private static final String CONST_CONTEXT_PATH = "/test";
    private static final String CONST_CONTEXT_PATH_2 = "/test1";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private InitialFlowSetupAction action;
    private CookieRetrievingCookieGenerator warnCookieGenerator;
    private CookieRetrievingCookieGenerator tgtCookieGenerator;

    @Before
    public void setUp() throws Exception {
        this.warnCookieGenerator = new CookieRetrievingCookieGenerator("warn", "", 2,
                false, null, false);
        this.warnCookieGenerator.setCookiePath(StringUtils.EMPTY);
        this.tgtCookieGenerator = new CookieRetrievingCookieGenerator("tgt", "", 2, 
                false, null, false);
        this.tgtCookieGenerator.setCookiePath(StringUtils.EMPTY);

        final List<ArgumentExtractor> argExtractors = Collections.singletonList(new DefaultArgumentExtractor(new WebApplicationServiceFactory()));
        final ServicesManager servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(RegisteredServiceTestUtils.getRegisteredService("test"));
        this.action = new InitialFlowSetupAction(argExtractors, servicesManager, authenticationRequestServiceSelectionStrategies, tgtCookieGenerator,
                warnCookieGenerator, casProperties);

        this.action.afterPropertiesSet();
    }

    @Test
    public void verifySettingContextPath() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(CONST_CONTEXT_PATH);
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        this.action.doExecute(context);

        assertEquals(CONST_CONTEXT_PATH + '/', this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH + '/', this.tgtCookieGenerator.getCookiePath());
    }

    @Test
    public void verifyResettingContextPath() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(CONST_CONTEXT_PATH);
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        this.action.doExecute(context);

        assertEquals(CONST_CONTEXT_PATH + '/', this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH + '/', this.tgtCookieGenerator.getCookiePath());

        request.setContextPath(CONST_CONTEXT_PATH_2);
        this.action.doExecute(context);

        assertNotSame(CONST_CONTEXT_PATH_2 + '/', this.warnCookieGenerator.getCookiePath());
        assertNotSame(CONST_CONTEXT_PATH_2 + '/', this.tgtCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH + '/', this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH + '/', this.tgtCookieGenerator.getCookiePath());
    }
}
