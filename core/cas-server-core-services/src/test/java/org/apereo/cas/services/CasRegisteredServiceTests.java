package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link CasRegisteredService}.
 *
 * @author Marvin S. Addison
 * @since 3.4.0
 */
@Tag("RegisteredService")
@Execution(ExecutionMode.SAME_THREAD)
class CasRegisteredServiceTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    public static Stream<Arguments> getParameters() {
        val domainCatchallHttp = "https*://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        val domainCatchallHttpImap = "(https*|imaps*)://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        val globalCatchallHttpImap = "(https*|imaps*)://.*";
        return Stream.of(
            arguments(
                newService(domainCatchallHttp),
                "https://service.vt.edu/webapp?a=1",
                true
            ),
            arguments(
                newService(domainCatchallHttp),
                "http://test-01.service.vt.edu/webapp?a=1",
                true
            ),
            arguments(
                newService(domainCatchallHttp),
                "https://thepiratebay.se?service.vt.edu/webapp?a=1",
                false
            ),
            arguments(
                newService(domainCatchallHttpImap),
                "http://test_service.vt.edu/login",
                true
            ),
            arguments(
                newService(domainCatchallHttpImap),
                "imaps://imap-server-01.vt.edu/",
                true
            ),
            arguments(
                newService(globalCatchallHttpImap),
                "https://host-01.example.com/",
                true
            ),
            arguments(
                newService(globalCatchallHttpImap),
                "imap://host-02.example.edu/",
                true
            ),
            arguments(
                newService(globalCatchallHttpImap),
                null,
                false
            )
        );
    }

    private static RegisteredService newService(final String id) {
        val service = new CasRegisteredService();
        service.setServiceId(id);
        service.setName(UUID.randomUUID().toString());
        service.setId(RandomUtils.nextLong());
        service.setLogoutType(RegisteredServiceLogoutType.FRONT_CHANNEL);
        service.setServiceTicketExpirationPolicy(
            new DefaultRegisteredServiceServiceTicketExpirationPolicy(100, "100"));
        service.setProxyTicketExpirationPolicy(
            new DefaultRegisteredServiceProxyTicketExpirationPolicy(100, "100"));
        val policy = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        policy.addPolicies(Arrays.asList(
            new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 100, 1),
            new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 100, 1)));
        service.setSingleSignOnParticipationPolicy(policy);

        val consent = new DefaultRegisteredServiceConsentPolicy(CollectionUtils.wrapSet("attr1", "attr2"),
            CollectionUtils.wrapSet("ex-attr1", "ex-attr2"));
        consent.setStatus(TriStateBoolean.TRUE);

        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        attrPolicy.setConsentPolicy(consent);
        service.setAttributeReleasePolicy(attrPolicy);
        return service;
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    void verifyMatches(final CasRegisteredService service, final String serviceToMatch, final boolean expectedResult) throws Exception {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val testService = Optional.ofNullable(serviceToMatch).map(RegisteredServiceTestUtils::getService).orElse(null);
        assertEquals(expectedResult, service.matches(testService), () -> "Service: " + service + " cannot match " + testService);
        MAPPER.writeValue(jsonFile, service);
        val serviceRead = MAPPER.readValue(jsonFile, CasRegisteredService.class);
        assertEquals(service, serviceRead);
        assertEquals(expectedResult, serviceRead.matches(testService));
    }

    @Test
    void verifyDefaultMatchingStrategy() {
        val service = new CasRegisteredService();
        service.setMatchingStrategy(null);
        service.setServiceId("\\d\\d\\d");
        assertFalse(service.matches("https://google123.com"));
    }

    @Test
    void verifyDefaults() {
        val service = mock(RegisteredService.class);
        when(service.getDescription()).thenCallRealMethod();
        when(service.getFriendlyName()).thenCallRealMethod();
        doCallRealMethod().when(service).initialize();

        assertNotNull(service.getDescription());
        assertNotNull(service.getFriendlyName());
        assertDoesNotThrow(service::initialize);
    }

}
