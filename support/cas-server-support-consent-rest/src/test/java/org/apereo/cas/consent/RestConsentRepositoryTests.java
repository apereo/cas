package org.apereo.cas.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.ExpectedCount.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

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
    public void verifyConsentDecisionIsFound() {
        final ObjectMapper mapper = new ObjectMapper();
        final org.apereo.cas.consent.ConsentDecisionBuilder
        final ConsentDecision decision =
        server.expect(manyTimes(), requestTo("/consent"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
    }
}
