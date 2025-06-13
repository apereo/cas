package org.apereo.cas.services.web;

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
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
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
    void verifyOperation() throws Throwable {
        MockRequestContext.create(applicationContext);

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
