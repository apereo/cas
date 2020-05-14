package org.apereo.cas.services;

import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Unit test for {@link RegexRegisteredService}.
 *
 * @author Marvin S. Addison
 * @since 3.4.0
 */
@Tag("Simple")
public class RegexRegisteredServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "regexRegisteredService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .findAndRegisterModules();

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

    private static RegexRegisteredService newService(final String id) {
        val service = new RegexRegisteredService();
        service.setServiceId(id);
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
        consent.setEnabled(true);

        val attrPolicy = new ReturnAllowedAttributeReleasePolicy();
        attrPolicy.setConsentPolicy(consent);
        service.setAttributeReleasePolicy(attrPolicy);
        return service;
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyMatches(final RegexRegisteredService service,
                              final String serviceToMatch,
                              final boolean expectedResult) {
        val testService = Optional.ofNullable(serviceToMatch).map(RegisteredServiceTestUtils::getService).orElse(null);
        assertEquals(expectedResult, service.matches(testService));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifySerialization(final RegexRegisteredService service,
                                    final String serviceToMatch,
                                    final boolean expectedResult) throws IOException {
        MAPPER.writeValue(JSON_FILE, service);
        val serviceRead = MAPPER.readValue(JSON_FILE, RegexRegisteredService.class);
        assertEquals(service, serviceRead);
        val testService = Optional.ofNullable(serviceToMatch).map(RegisteredServiceTestUtils::getService).orElse(null);
        assertEquals(expectedResult, serviceRead.matches(testService));
    }

}
