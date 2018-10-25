package org.apereo.cas.consent;

import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.config.CasConsentRestConfiguration;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * This is {@link RestConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Category(RestfulApiCategory.class)
@SpringBootTest(classes = {CasConsentRestConfiguration.class})
public class RestConsentRepositoryTests extends BaseConsentRepositoryTests {
    private static final String CONSENT = "/consent";
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final Function<ConsentDecision, String> HANDLER = Unchecked.function(MAPPER::writeValueAsString);

    private final Map<String, ConsentRepository> repos = new HashMap<>();

    @Override
    public ConsentRepository getRepository() {
        return getRepository("default");
    }

    @Override
    public ConsentRepository getRepository(final String testName) {
        return repos.computeIfAbsent(testName, n -> new RestConsentRepository(new RestTemplate(), CONSENT));
    }

    private static MockRestServiceServer getNewServer(final RestConsentRepository repository) {
        return MockRestServiceServer.bindTo(repository.getRestTemplate()).build();
    }

    @Test
    @Override
    public void verifyConsentDecisionIsNotFound() {

        val decision = BUILDER.build(SVC, REG_SVC, "casuser", CollectionUtils.wrap("attribute", "value"));
        val body = HANDLER.apply(decision);
        val repo = getRepository("verifyConsentDecisionIsNotFound");
        val server = getNewServer((RestConsentRepository) repo);
        server.expect(manyTimes(), requestTo(CONSENT))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        val exp = server.expect(manyTimes(), requestTo(CONSENT));
        assertNotNull(exp);
        exp.andExpect(method(HttpMethod.GET))
            .andRespond(withServerError());
        super.verifyConsentDecisionIsNotFound();
        server.verify();
    }

    @Test
    @Override
    public void verifyConsentDecisionIsFound() {

        val decision = BUILDER.build(SVC, REG_SVC, "casuser2", CollectionUtils.wrap("attribute", "value"));
        decision.setId(100);
        val body = HANDLER.apply(decision);
        val repo = getRepository("verifyConsentDecisionIsFound");
        val server = getNewServer((RestConsentRepository) repo);
        server.expect(once(), requestTo(CONSENT))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(CONSENT))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("/consent/100"))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withSuccess());
        val exp = server.expect(once(), requestTo(CONSENT));
        assertNotNull(exp);
        exp.andExpect(method(HttpMethod.GET))
            .andRespond(withServerError());

        super.verifyConsentDecisionIsFound();
        server.verify();
    }
}
