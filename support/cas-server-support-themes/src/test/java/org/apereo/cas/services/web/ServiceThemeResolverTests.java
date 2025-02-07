package org.apereo.cas.services.web;

import org.apereo.cas.BaseThemeTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ThemeResolver;
import static org.junit.jupiter.api.Assertions.*;

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
@ExtendWith(CasTestExtension.class)
class ServiceThemeResolverTests {
    private static final String MOZILLA = "Mozilla";

    private static final String DEFAULT_THEME_NAME = "test";

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("themeResolver")
    private ThemeResolver themeResolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyGetServiceThemeDoesNotExist() throws Throwable {
        val registeredService = new CasRegisteredService();
        registeredService.setTheme("myTheme");
        registeredService.setId(1000);
        registeredService.setName("Test Service");
        registeredService.setServiceId("myServiceId");

        servicesManager.save(registeredService);

        val context = MockRequestContext.create(applicationContext);
        context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(registeredService.getServiceId()));
        context.withUserAgent();
        assertEquals(DEFAULT_THEME_NAME, themeResolver.resolveThemeName(context.getHttpServletRequest()));
    }

    @Test
    void verifyGetDefaultService() {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "myServiceId");
        request.addHeader(HttpHeaders.USER_AGENT, MOZILLA);
        assertEquals(DEFAULT_THEME_NAME, this.themeResolver.resolveThemeName(request));
    }
}
