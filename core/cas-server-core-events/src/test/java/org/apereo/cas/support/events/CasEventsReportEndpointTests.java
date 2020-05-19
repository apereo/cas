package org.apereo.cas.support.events;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.config.CasCoreEventsConfiguration;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.web.CasEventsReportEndpoint;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.security.auth.login.FailedLoginException;

import java.util.Collection;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEventsReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    CasEventsReportEndpointTests.CasEventsReportEndpointTestConfiguration.class,
    CasCoreEventsConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class CasEventsReportEndpointTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    public void initialize() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.100");
        request.setLocalAddr("223.456.789.100");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }

    @Test
    public void verifyOperation() {
        val event = new CasAuthenticationTransactionFailureEvent(this,
            CollectionUtils.wrap("error", new FailedLoginException()),
            CollectionUtils.wrap(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        applicationContext.publishEvent(event);
        assertFalse(casEventRepository.load().isEmpty());

        val endpoint = new CasEventsReportEndpoint(casProperties, casEventRepository);
        val result = endpoint.events();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @TestConfiguration("CasEventsReportEndpointTestConfiguration")
    @Lazy(false)
    public static class CasEventsReportEndpointTestConfiguration {
        @Bean
        public CasEventRepository casEventRepository() {
            return new AbstractCasEventRepository(CasEventRepositoryFilter.noOp()) {
                private final Collection<CasEvent> events = new LinkedHashSet<>();

                @Override
                public void saveInternal(final CasEvent event) {
                    events.add(event);
                }

                @Override
                public Collection<CasEvent> load() {
                    return events;
                }
            };
        }
    }
}
