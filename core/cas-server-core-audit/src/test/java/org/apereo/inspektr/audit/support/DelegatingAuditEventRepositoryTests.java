package org.apereo.inspektr.audit.support;

import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import lombok.val;
import module java.base;
import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.test.CasTestExtension;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatingAuditEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Audits")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    DelegatingAuditEventRepositoryTests.AuditEventRepositoryTestConfiguration.class,
    BaseAuditConfigurationTests.SharedTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DelegatingAuditEventRepositoryTests {
    @Autowired
    @Qualifier("auditEventRepository")
    private AuditEventRepository auditEventRepository;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository casEventRepository;

    @BeforeEach
    void setup() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.000");
        request.setLocalAddr("223.456.789.100");
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyOperation() {
        auditEventRepository.add(new AuditEvent("casuser", "success", Map.of()));
        auditEventRepository.add(new AuditEvent("casuser", "failure", Map.of()));
        assertEquals(1, auditEventRepository.find("casuser", null, "success").size());
        assertEquals(2, auditEventRepository.find("casuser", Instant.now().minusSeconds(10), null).size());
        assertEquals(1, auditEventRepository.find("casuser", Instant.now().minusSeconds(10), "failure").size());
        assertEquals(2, auditEventRepository.find("casuser", null, null).size());
        assertEquals(1, auditEventRepository.find(null, Instant.now().minusSeconds(10), "failure").size());
        assertEquals(2, auditEventRepository.find("casuser", null, null).size());
        assertEquals(1, auditEventRepository.find(null, null, "success").size());
        assertTrue(auditEventRepository.find("unknown", null, "success").isEmpty());
    }

    @Test
    void verifyEventSource() {
        val data = new HashMap<String, @Nullable Object>();
        data.put(CasEventRepository.PARAM_SOURCE, null);
        auditEventRepository.add(new AuditEvent("nosource", "eventsource", data));
        auditEventRepository.add(new AuditEvent("othersource", "eventsource", Map.of(CasEventRepository.PARAM_SOURCE, "Spring")));
        auditEventRepository.add(new AuditEvent("cassource", "eventsource", Map.of(CasEventRepository.PARAM_SOURCE, "cas")));
        assertEquals(1, casEventRepository.load().filter(event -> "nosource".equals(event.getPrincipalId())).count());
        assertEquals(1, casEventRepository.load().filter(event -> "othersource".equals(event.getPrincipalId())).count());
        assertEquals(0, casEventRepository.load().filter(event -> "cassource".equals(event.getPrincipalId())).count());
        assertEquals(1, auditEventRepository.find("cassource", null, null).size());
    }

    @TestConfiguration(value = "AuditEventRepositoryTestConfiguration", proxyBeanMethods = false)
    public static class AuditEventRepositoryTestConfiguration {
        @Bean
        public CasEventRepository casEventRepository() {
            return new AbstractCasEventRepository(CasEventRepositoryFilter.noOp()) {
                private final Collection<CasEvent> events = new LinkedHashSet<>();

                @Override
                public CasEvent saveInternal(final CasEvent event) {
                    events.add(event);
                    return event;
                }

                @Override
                public void removeAll() {
                    events.clear();
                }

                @Override
                public Stream<CasEvent> load() {
                    return events.stream();
                }
            };
        }
    }

}
