package org.apereo.cas.support.events.listener;

import org.apereo.cas.config.CasCoreEventsConfigEnvironmentConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.config.CasCoreConfigurationWatchConfiguration;
import org.apereo.cas.configuration.config.CasCoreEnvironmentConfiguration;
import org.apereo.cas.configuration.config.standalone.CasCoreBootstrapStandaloneConfiguration;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasConfigurationEventListenerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    CasCoreEventsConfigEnvironmentConfiguration.class,
    CasCoreConfigurationWatchConfiguration.class,
    CasCoreBootstrapStandaloneConfiguration.class,
    CasCoreEnvironmentConfiguration.class,

    DispatcherServletAutoConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "spring.application.name=cas",
    "spring.profiles.active=standalone",
    "spring.cloud.config.enabled=false"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("CasConfiguration")
public class CasConfigurationEventListenerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("casConfigurationEventListener")
    private CasConfigurationEventListener casConfigurationEventListener;

    @Test
    public void verifyOperation() {
        assertNotNull(casConfigurationEventListener);
        assertDoesNotThrow(() -> applicationContext.publishEvent(
            new EnvironmentChangeEvent(Set.of("cas.server.name"))));
        assertDoesNotThrow(() -> applicationContext.publishEvent(
            new CasConfigurationModifiedEvent(this, true)));
        assertDoesNotThrow(() -> applicationContext.publishEvent(
            new RefreshScopeRefreshedEvent()));
    }

    @Test
    public void verifyDispatcherServletIsNotInitialized() {
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val dispatcherServlet = new DispatcherServlet();
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[] { "dispatcherServlet" });
        when(applicationContext.getBean("dispatcherServlet")).thenReturn(dispatcherServlet);
        when(applicationContext.containsBean("dispatcherServlet")).thenReturn(true);
        when(applicationContext.getBean(DispatcherServlet.class)).thenReturn(dispatcherServlet);

        val listener = new DefaultCasConfigurationEventListener(null, null, null, applicationContext);
        assertDoesNotThrow(() -> listener.onRefreshScopeRefreshed(new RefreshScopeRefreshedEvent()));

        verify(applicationContext).getBean("dispatcherServlet");
        verify(applicationContext, never()).containsBean("dispatcherServlet");
        verify(applicationContext, never()).getBean(DispatcherServlet.class);
    }
}
