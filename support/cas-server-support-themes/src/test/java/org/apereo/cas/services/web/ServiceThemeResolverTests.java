package org.apereo.cas.services.web;

import org.apereo.cas.BaseThemeTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@SpringBootTest(classes = BaseThemeTests.SharedTestConfiguration.class,
    properties = {
        "cas.view.template-prefixes[0]=file:///etc/cas/templates",
        "cas.theme.default-theme-name=test"
    })
@Tag("Web")
class ServiceThemeResolverTests {
    private static final String MOZILLA = "Mozilla";

    private static final String DEFAULT_THEME_NAME = "test";

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("themeResolver")
    private ThemeResolver themeResolver;

    @Test
    void verifyGetServiceThemeDoesNotExist() throws Throwable {
        val r = new CasRegisteredService();
        r.setTheme("myTheme");
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("myServiceId");

        servicesManager.save(r);

        val request = new MockHttpServletRequest();
        val ctx = mock(RequestContext.class);
        val scope = new LocalAttributeMap<>();
        scope.put(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(r.getServiceId()));
        when(ctx.getFlowScope()).thenReturn(scope);
        RequestContextHolder.setRequestContext(ctx);
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, MOZILLA);
        assertEquals(DEFAULT_THEME_NAME, themeResolver.resolveThemeName(request));
    }

    @Test
    void verifyGetDefaultService() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "myServiceId");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, MOZILLA);
        assertEquals(DEFAULT_THEME_NAME, this.themeResolver.resolveThemeName(request));
    }
}
