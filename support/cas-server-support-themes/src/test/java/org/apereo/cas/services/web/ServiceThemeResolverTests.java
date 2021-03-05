package org.apereo.cas.services.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = {
    CasThemesConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreUtilConfiguration.class,
    WebMvcAutoConfiguration.class,
    ThymeleafAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.view.template-prefixes[0]=file:///etc/cas/templates",
        "cas.theme.default-theme-name=test"
    })
@Tag("Web")
public class ServiceThemeResolverTests {
    private static final String MOZILLA = "Mozilla";

    private static final String DEFAULT_THEME_NAME = "test";

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("themeResolver")
    private ThemeResolver themeResolver;

    @Test
    public void verifyGetServiceThemeDoesNotExist() {
        val r = new RegexRegisteredService();
        r.setTheme("myTheme");
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("myServiceId");

        this.servicesManager.save(r);

        val request = new MockHttpServletRequest();
        val ctx = mock(RequestContext.class);
        val scope = new LocalAttributeMap<>();
        scope.put(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(r.getServiceId()));
        when(ctx.getFlowScope()).thenReturn(scope);
        RequestContextHolder.setRequestContext(ctx);
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, MOZILLA);
        assertEquals(DEFAULT_THEME_NAME, this.themeResolver.resolveThemeName(request));
    }

    @Test
    public void verifyGetDefaultService() {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "myServiceId");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, MOZILLA);
        assertEquals(DEFAULT_THEME_NAME, this.themeResolver.resolveThemeName(request));
    }
}
