package org.apereo.cas.services.web;

import org.apereo.cas.BaseThymeleafTests;
import org.apereo.cas.configuration.model.support.themes.ThemeProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.validation.CasProtocolViewFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;
import org.thymeleaf.spring6.view.AbstractThymeleafView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import java.util.Locale;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasThymeleafViewResolverConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = {
    BaseThymeleafTests.SharedTestConfiguration.class,
    CasThymeleafViewResolverConfigurerTests.CasThymeleafViewResolverConfigurerTestConfiguration.class
}, properties = "cas.custom.properties.test=test")
@Tag("Web")
@ExtendWith(CasTestExtension.class)
class CasThymeleafViewResolverConfigurerTests {

    @Autowired
    @Qualifier("thymeleafViewResolver")
    private ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    @Qualifier("themeViewResolverFactory")
    private ThemeViewResolverFactory themeViewResolverFactory;

    @Test
    void verifyOperation() throws Throwable {
        val view = thymeleafViewResolver.resolveViewName("testTemplate", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(((AbstractThymeleafView) view).getStaticVariables().containsKey("cas"));
        assertTrue(((AbstractThymeleafView) view).getStaticVariables().containsKey("casProperties"));

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        view.render(Map.of(), request, response);
        val body = response.getContentAsString();
        assertNotNull(body);
    }

    @Test
    void verifyDirectView() throws Throwable {
        val resolver = themeViewResolverFactory.create(ThemeProperties.DEFAULT_THEME_NAME);
        val view = resolver.resolveViewName("oneCustomView", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(((AbstractThymeleafView) view).getStaticVariables().containsKey("cas"));
        assertTrue(((AbstractThymeleafView) view).getStaticVariables().containsKey("casProperties"));
    }

    @TestConfiguration(value = "CasThymeleafViewResolverConfigurerTestConfiguration", proxyBeanMethods = false)
    static class CasThymeleafViewResolverConfigurerTestConfiguration {
        @Autowired
        @Qualifier(CasProtocolViewFactory.BEAN_NAME_THYMELEAF_VIEW_FACTORY)
        private CasProtocolViewFactory casProtocolViewFactory;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Bean
        public View oneCustomView() {
            return casProtocolViewFactory.create(applicationContext, "testTemplate");
        }
    }
}
