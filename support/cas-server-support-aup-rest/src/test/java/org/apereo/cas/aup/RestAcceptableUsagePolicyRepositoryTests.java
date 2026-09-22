package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.config.CasAcceptableUsagePolicyRestAutoConfiguration;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasAcceptableUsagePolicyRestAutoConfiguration.class,
    BaseAcceptableUsagePolicyRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "cas.acceptable-usage-policy.rest.url=https://localhost/aup",
    "cas.acceptable-usage-policy.core.aup-attribute-name=givenName"
})
class RestAcceptableUsagePolicyRepositoryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
    private AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Autowired
    @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
    private HttpClient httpClient;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyRepositoryIsResolvedFromConfiguration() {
        assertInstanceOf(RestAcceptableUsagePolicyRepository.class, acceptableUsagePolicyRepository);
    }

    @Test
    void verify() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setPreferredLocales(Locale.GERMAN);

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
        try (val webServer = new MockWebServer(StringUtils.EMPTY, HttpStatus.BAD_REQUEST)) {
            webServer.start();
            assertFalse(repositoryFor(webServer).verify(context).isAccepted());
        }
        try (val webServer = new MockWebServer(StringUtils.EMPTY, HttpStatus.ACCEPTED)) {
            webServer.start();
            assertTrue(repositoryFor(webServer).submit(context));
        }
    }

    @Test
    void verifyFails() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setPreferredLocales(Locale.GERMAN);

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        try (val webServer = new MockWebServer(StringUtils.EMPTY, HttpStatus.BAD_REQUEST)) {
            webServer.start();
            val repository = repositoryFor(webServer);
            assertFalse(repository.fetchPolicy(context).isPresent());
            assertFalse(repository.submit(context));
        }
    }

    @Test
    void verifyFetch() throws Throwable {
        val input = AcceptableUsagePolicyTerms.builder()
            .code("example")
            .defaultText("hello world")
            .build();
        val data = MAPPER.writeValueAsString(input);
        try (val webServer = new MockWebServer(data)) {
            webServer.start();
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
            val terms = repositoryFor(webServer).fetchPolicy(context);
            assertTrue(terms.isPresent());
            assertEquals(terms.get(), input);
        }
    }

    private RestAcceptableUsagePolicyRepository repositoryFor(final MockWebServer webServer) {
        val properties = new AcceptableUsagePolicyProperties();
        properties.getCore().setAupAttributeName("givenName");
        properties.getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
        return new RestAcceptableUsagePolicyRepository(httpClient, properties);
    }
}
