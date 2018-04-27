package org.apereo.cas.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * This is {@link RestConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RestConsentRepositoryTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    @Before
    public void before() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void verifyConsentDecisionIsNotFound() {
        server.expect(manyTimes(), requestTo("/consent"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withServerError());

        final AbstractRegisteredService regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        final Service svc = RegisteredServiceTestUtils.getService();
        final RestConsentRepository repo = new RestConsentRepository(this.restTemplate, "/consent");
        final ConsentDecision d = repo.findConsentDecision(svc, regSvc, CoreAuthenticationTestUtils.getAuthentication());
        assertNull(d);
        server.verify();
    }

    @Test
    public void verifyConsentDecisionsFound() throws Exception {

        final DefaultConsentDecisionBuilder builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        final AbstractRegisteredService regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        final Service svc = RegisteredServiceTestUtils.getService();
        final ConsentDecision decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));
        final String body = MAPPER.writeValueAsString(CollectionUtils.wrapList(decision));
        server.expect(manyTimes(), requestTo("/consent"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        final RestConsentRepository repo = new RestConsentRepository(this.restTemplate, "/consent");
        Collection<ConsentDecision> d = repo.findConsentDecisions("casuser");
        assertNotNull(d);
        assertFalse(d.isEmpty());
        server.verify();

        d = repo.findConsentDecisions();
        assertNotNull(d);
        assertFalse(d.isEmpty());
        server.verify();
    }

    @Test
    public void verifyConsentDecisionIsFound() throws Exception {
        final DefaultConsentDecisionBuilder builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        final AbstractRegisteredService regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        final Service svc = RegisteredServiceTestUtils.getService();
        final ConsentDecision decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));
        final String body = MAPPER.writeValueAsString(decision);
        server.expect(manyTimes(), requestTo("/consent"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        final RestConsentRepository repo = new RestConsentRepository(this.restTemplate, "/consent");
        final ConsentDecision d = repo.findConsentDecision(svc, regSvc, CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(d);
        assertEquals("casuser", d.getPrincipal());
        server.verify();
    }

    @Test
    public void verifyConsentDecisionStored() throws Exception {
        final DefaultConsentDecisionBuilder builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        final AbstractRegisteredService regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        final Service svc = RegisteredServiceTestUtils.getService();
        final ConsentDecision decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));
        final String body = MAPPER.writeValueAsString(decision);
        server.expect(manyTimes(), requestTo("/consent"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        final RestConsentRepository repo = new RestConsentRepository(this.restTemplate, "/consent");
        assertTrue(repo.storeConsentDecision(decision));
        server.verify();
    }

    @Test
    public void verifyConsentDecisionIsDeleted() {
        server.expect(manyTimes(), requestTo("/consent/1"))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withSuccess());

        final RestConsentRepository repo = new RestConsentRepository(this.restTemplate, "/consent");
        final boolean b = repo.deleteConsentDecision(1, "CasUser");
        assertTrue(b);
        server.verify();
    }
}
