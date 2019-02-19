package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.login.InitialFlowSetupAction;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.DefaultArgumentExtractor;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link InitialFlowSetupActionCookieTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InitialFlowSetupActionCookieTests extends AbstractWebflowActionsTests {

    private static final String CONST_CONTEXT_PATH = "/test";
    private static final String CONST_CONTEXT_PATH_2 = "/test1";

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private InitialFlowSetupAction action;
    private CookieRetrievingCookieGenerator warnCookieGenerator;
    private CookieRetrievingCookieGenerator tgtCookieGenerator;

    @BeforeEach
    public void initialize() throws Exception {
        this.warnCookieGenerator = new CookieRetrievingCookieGenerator("warn", "", 2,
            false, null, false);
        this.warnCookieGenerator.setCookiePath(StringUtils.EMPTY);
        this.tgtCookieGenerator = new CookieRetrievingCookieGenerator("tgt", "", 2,
            false, null, false);
        this.tgtCookieGenerator.setCookiePath(StringUtils.EMPTY);

        val argExtractors = Collections.<ArgumentExtractor>singletonList(new DefaultArgumentExtractor(new WebApplicationServiceFactory()));
        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(RegisteredServiceTestUtils.getRegisteredService("test"));
        this.action = new InitialFlowSetupAction(argExtractors, servicesManager,
            authenticationRequestServiceSelectionStrategies, tgtCookieGenerator,
            warnCookieGenerator, casProperties, authenticationEventExecutionPlan);

        this.action.afterPropertiesSet();
    }

    @Test
    public void verifySettingContextPath() {
        val request = new MockHttpServletRequest();
        request.setContextPath(CONST_CONTEXT_PATH);
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        this.action.doExecute(context);

        assertEquals(CONST_CONTEXT_PATH + '/', this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH + '/', this.tgtCookieGenerator.getCookiePath());
    }

    @Test
    public void verifyResettingContextPath() {
        val request = new MockHttpServletRequest();
        request.setContextPath(CONST_CONTEXT_PATH);
        val context = new MockRequestContext();
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
