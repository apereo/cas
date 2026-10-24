package org.apereo.cas.services.web;

import module java.base;
import org.apereo.cas.BaseThymeleafTests;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasThymeleafTemplatesDirectorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Web")
@SpringBootTest(classes = {
    CasThymeleafTemplatesDirectorTests.CasThymeleafTemplatesDirectorTestConfiguration.class,
    BaseThymeleafTests.SharedTestConfiguration.class
})
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CasThymeleafTemplatesDirectorTests {

    private static final String LOGIN_URL = "http://localhost:8080/cas/login";
    private static final String LOGIN_URL_WITH_CLIENT_NAME = "http://localhost:8080/cas/login?client_name=Client1";

    @Autowired
    @Qualifier("casThymeleafTemplatesDirector")
    private CasThymeleafTemplatesDirector director;

    @Autowired
    @Qualifier("casThymeleafExpressionDialect")
    private IExpressionObjectDialect casThymeleafExpressionDialect;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    @SuppressWarnings("unchecked")
    void verifyOperation() throws Throwable {
        MockRequestContext.create(applicationContext);

        val themeMessage = director.theme("cas.theme.description");
        assertEquals("Central Authentication Service", themeMessage);
        assertTrue(director.isThemeBuilderEnabled());
        assertEquals(10, director.themeInteger("cas.login-providers.select-threshold", 5));
        assertEquals(250, director.themeInteger("cas.qr-code.size", 100));
        assertEquals(7, director.themeInteger("cas.theme.description", 7));
        assertEquals(3, director.themeInteger("cas.unknown.property", 3));

        val themeBuilder = director.getThemeBuilder();
        assertEquals("cas-theme-default", themeBuilder.get("theme"));
        val properties = (List<Map<String, String>>) themeBuilder.get("properties");
        val byName = properties.stream().collect(Collectors.toMap(entry -> entry.get("name"), Function.identity()));
        assertEquals(properties.size(), byName.size());
        assertEquals("true", byName.get(CasThymeleafTemplatesDirector.THEME_PROPERTY_THEME_BUILDER_ENABLED).get("value"));
        assertEquals("true", byName.get(CasThymeleafTemplatesDirector.THEME_PROPERTY_THEME_BUILDER_ENABLED).get("defaultValue"));
        assertTrue(byName.get(CasThymeleafTemplatesDirector.THEME_PROPERTY_THEME_BUILDER_ENABLED).get("description").contains("theme builder"));
        val themeName = byName.get("cas.theme.name");
        assertEquals(StringUtils.EMPTY, themeName.get("defaultValue"));
        assertEquals("CAS", themeName.get("example"));
        assertEquals("Default theme name", themeName.get("description"));
        assertEquals("Central Authentication Service", byName.get("cas.theme.description").get("value"));
        assertEquals("en,de,fr,it,es,ar,fa,ja,nl,pl,ru,sk,tr,pt_BR,zh_CN,zh_TW",
            byName.get("cas.locale-selection.languages").get("defaultValue"));
        
        assertNotNull(director.getExceptionClassSimpleName(new AuthenticationException()));
        assertEquals(LOGIN_URL + '?', director.getUrlExternalForm(new URI(LOGIN_URL).toURL()));
        assertEquals(LOGIN_URL_WITH_CLIENT_NAME + '&', director.getUrlExternalForm(new URI(LOGIN_URL_WITH_CLIENT_NAME).toURL()));

        val webEngine = mock(WebEngineContext.class);
        assertTrue(director.isTrue("on"));
        assertFalse(director.isTrue(StringUtils.EMPTY));
        assertTrue(director.isLoginFormViewable(webEngine));
        assertTrue(director.isLoginFormUsernameInputVisible(webEngine));
        assertFalse(director.isLoginFormUsernameInputDisabled(webEngine));
        assertFalse(director.getLoginFormUsername(webEngine).isEmpty());
        assertNotNull(director.format(LocalDateTime.now(Clock.systemUTC()), "yyyy/mm/dd"));
        assertNotNull(director.sort(new Object[] {"a", "v", "c"}));

        assertNotNull(casThymeleafExpressionDialect.getName());

        val expressionObjectFactory = casThymeleafExpressionDialect.getExpressionObjectFactory();
        assertNotNull(expressionObjectFactory);
        assertTrue(expressionObjectFactory.getAllExpressionObjectNames().contains("cas"));
        assertFalse(expressionObjectFactory.isCacheable("anything"));
        val casDirector = expressionObjectFactory.buildObject(webEngine, "cas");
        assertNotNull(casDirector);
    }

    @TestConfiguration(value = "CasThymeleafTemplatesDirectorTestConfiguration", proxyBeanMethods = false)
    public static class CasThymeleafTemplatesDirectorTestConfiguration {
        @Bean
        public CasWebflowExecutionPlanConfigurer testCasWebflowLoginContextProvider() {
            return plan -> {
                val provider = mock(CasWebflowLoginContextProvider.class);
                when(provider.getOrder()).thenCallRealMethod();
                when(provider.isLoginFormUsernameInputDisabled(any())).thenReturn(false);
                when(provider.getCandidateUsername(any())).thenReturn(Optional.of("cas"));
                plan.registerWebflowLoginContextProvider(provider);
            };
        }
    }
}
