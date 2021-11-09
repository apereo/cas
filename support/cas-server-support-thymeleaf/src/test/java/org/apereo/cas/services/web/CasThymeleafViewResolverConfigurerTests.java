package org.apereo.cas.services.web;

import org.apereo.cas.BaseThymeleafTests;
import org.apereo.cas.validation.CasProtocolViewFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.View;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;
import org.thymeleaf.spring5.view.ThymeleafView;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

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
}, properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "cas.custom.properties.test=test"
})
@Tag("Web")
public class CasThymeleafViewResolverConfigurerTests {

    @Autowired
    @Qualifier("thymeleafViewResolver")
    private ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    @Qualifier("themeViewResolverFactory")
    private ThemeViewResolverFactory themeViewResolverFactory;

    @Test
    public void verifyOperation() throws Exception {
        val view = thymeleafViewResolver.resolveViewName("testTemplate", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(((ThymeleafView) view).getStaticVariables().containsKey("cas"));
        assertTrue(((ThymeleafView) view).getStaticVariables().containsKey("casProperties"));

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        view.render(Map.of(), request, response);
        val body = response.getContentAsString();
        assertNotNull(body);
    }

    @Test
    public void verifyDirectView() throws Exception {
        val resolver = themeViewResolverFactory.create("cas-theme-default");
        val view = resolver.resolveViewName("oneCustomView", Locale.ENGLISH);
        assertNotNull(view);
        assertTrue(((ThymeleafView) view).getStaticVariables().containsKey("cas"));
        assertTrue(((ThymeleafView) view).getStaticVariables().containsKey("casProperties"));
    }

    @TestConfiguration
    public static class CasThymeleafViewResolverConfigurerTestConfiguration {
        @Autowired
        @Qualifier("casProtocolViewFactory")
        private CasProtocolViewFactory casProtocolViewFactory;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Bean
        public View oneCustomView() {
            return casProtocolViewFactory.create(applicationContext, "testTemplate");
        }
    }
}
