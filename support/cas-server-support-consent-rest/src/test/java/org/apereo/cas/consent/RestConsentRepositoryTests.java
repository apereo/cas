package org.apereo.cas.consent;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

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

        val regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        val svc = RegisteredServiceTestUtils.getService();
        val repo = new RestConsentRepository(this.restTemplate, "/consent");
        val d = repo.findConsentDecision(svc, regSvc, CoreAuthenticationTestUtils.getAuthentication());
        assertNull(d);
        server.verify();
    }

    @Test
    public void verifyConsentDecisionsFound() throws Exception {

        val builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        val regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        val svc = RegisteredServiceTestUtils.getService();
        val decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));
        val body = MAPPER.writeValueAsString(CollectionUtils.wrapList(decision));
        server.expect(manyTimes(), requestTo("/consent"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        val repo = new RestConsentRepository(this.restTemplate, "/consent");
        var d = repo.findConsentDecisions("casuser");
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
        val builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        val regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        val svc = RegisteredServiceTestUtils.getService();
        val decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));
        val body = MAPPER.writeValueAsString(decision);
        server.expect(manyTimes(), requestTo("/consent"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        val repo = new RestConsentRepository(this.restTemplate, "/consent");
        val d = repo.findConsentDecision(svc, regSvc, CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(d);
        assertEquals("casuser", d.getPrincipal());
        server.verify();
    }

    @Test
    public void verifyConsentDecisionStored() throws Exception {
        val builder = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
        val regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        val svc = RegisteredServiceTestUtils.getService();
        val decision = builder.build(svc,
            regSvc, "casuser",
            CollectionUtils.wrap("attribute", "value"));
        val body = MAPPER.writeValueAsString(decision);
        server.expect(manyTimes(), requestTo("/consent"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        val repo = new RestConsentRepository(this.restTemplate, "/consent");
        assertTrue(repo.storeConsentDecision(decision));
        server.verify();
    }

    @Test
    public void verifyConsentDecisionIsDeleted() {
        server.expect(manyTimes(), requestTo("/consent/1"))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withSuccess());

        val repo = new RestConsentRepository(this.restTemplate, "/consent");
        val b = repo.deleteConsentDecision(1, "CasUser");
        assertTrue(b);
        server.verify();
    }
}
