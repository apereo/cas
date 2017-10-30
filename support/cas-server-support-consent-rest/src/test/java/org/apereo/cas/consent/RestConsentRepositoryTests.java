package org.apereo.cas.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
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
    public void verifyConsentDecisionIsFound() throws Exception {
        final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        final DefaultConsentDecisionBuilder builder = new DefaultConsentDecisionBuilder(NoOpCipherExecutor.getInstance());
        final AbstractRegisteredService regSvc = RegisteredServiceTestUtils.getRegisteredService("test");
        final Service svc = RegisteredServiceTestUtils.getService();
        final ConsentDecision decision = builder.build(svc,
                regSvc, "casuser",
                CollectionUtils.wrap("attribute", "value"));
        final String body = mapper.writeValueAsString(decision);
        server.expect(manyTimes(), requestTo("/consent"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        final RestConsentRepository repo = new RestConsentRepository(this.restTemplate, "/consent");
        final ConsentDecision d = repo.findConsentDecision(svc, regSvc, CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(d);
        assertEquals(d.getPrincipal(), "casuser");
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
