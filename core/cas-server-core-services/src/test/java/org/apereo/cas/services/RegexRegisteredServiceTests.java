package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

/**
 * Unit test for {@link RegexRegisteredService}.
 *
 * @author Marvin S. Addison
 * @since 3.4.0
 */
public class RegexRegisteredServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "regexRegisteredService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Stream<Arguments> getParameters() {
        val domainCatchallHttp = "https*://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        val domainCatchallHttpImap = "(https*|imaps*)://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        val globalCatchallHttpImap = "(https*|imaps*)://.*";
        return Stream.of(
            // CAS-1071 domain-specific HTTP catch-all #1
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
            // Domain-specific catch-all for HTTP(S)/IMAP(S) #1
            arguments(
                newService(domainCatchallHttpImap),
                "http://test_service.vt.edu/login",
                true
            ),
            // Domain-specific catch-all for HTTP(S)/IMAP(S) #2
            arguments(
                newService(domainCatchallHttpImap),
                "imaps://imap-server-01.vt.edu/",
                true
                ),
            // Global catch-all for HTTP(S)/IMAP(S) #1
            arguments(
                newService(globalCatchallHttpImap),
                "https://host-01.example.com/",
                true
                ),
            // Global catch-all for HTTP(S)/IMAP(S) #2
            arguments(
                newService(globalCatchallHttpImap),
                "imap://host-02.example.edu/",
                true
            ),
            // Null case
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
        return service;
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifyMatches(final RegexRegisteredService service,
                              final String serviceToMatch,
                              final boolean expectedResult) {
        val testService = serviceToMatch == null ? null : RegisteredServiceTestUtils.getService(serviceToMatch);
        assertEquals(expectedResult, service.matches(testService));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifySerializeARegexRegisteredServiceToJson(final RegexRegisteredService service,
                                                             final String serviceToMatch,
                                                             final boolean expectedResult) throws IOException {
        MAPPER.writeValue(JSON_FILE, service);
        val serviceRead = MAPPER.readValue(JSON_FILE, RegexRegisteredService.class);
        assertEquals(service, serviceRead);
        val testService = serviceToMatch == null ? null : RegisteredServiceTestUtils.getService(serviceToMatch);
        assertEquals(expectedResult, serviceRead.matches(testService));
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void verifySerializeRegexRegisteredServiceWithLogoutToJson(final RegexRegisteredService service,
                                                             final String serviceToMatch,
                                                             final boolean expectedResult) throws IOException {
        service.setLogoutType(RegisteredServiceLogoutType.FRONT_CHANNEL);
        MAPPER.writeValue(JSON_FILE, service);
        val serviceRead = MAPPER.readValue(JSON_FILE, RegexRegisteredService.class);
        assertEquals(service, serviceRead);
        val testService = serviceToMatch == null ? null : RegisteredServiceTestUtils.getService(serviceToMatch);
        assertEquals(expectedResult, serviceRead.matches(testService));
    }
}
