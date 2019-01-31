package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link RegexRegisteredService}.
 *
 * @author Marvin S. Addison
 * @since 3.4.0
 */
@RunWith(Parameterized.class)
public class RegexRegisteredServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "regexRegisteredService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RegexRegisteredService service;

    private final String serviceToMatch;

    private final boolean expected;

    public RegexRegisteredServiceTests(
        final RegexRegisteredService service,
        final String serviceToMatch,
        final boolean expectedResult) {
        this.service = service;
        this.serviceToMatch = serviceToMatch;
        this.expected = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        val domainCatchallHttp = "https*://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        val domainCatchallHttpImap = "(https*|imaps*)://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        val globalCatchallHttpImap = "(https*|imaps*)://.*";
        return Arrays.asList(new Object[][]{
            // CAS-1071 domain-specific HTTP catch-all #1
            {
                newService(domainCatchallHttp),
                "https://service.vt.edu/webapp?a=1",
                true,
            },
            {
                newService(domainCatchallHttp),
                "http://test-01.service.vt.edu/webapp?a=1",
                true,
            },
            {
                newService(domainCatchallHttp),
                "https://thepiratebay.se?service.vt.edu/webapp?a=1",
                false,
            },
            // Domain-specific catch-all for HTTP(S)/IMAP(S) #1
            {
                newService(domainCatchallHttpImap),
                "http://test_service.vt.edu/login",
                true,
            },
            // Domain-specific catch-all for HTTP(S)/IMAP(S) #2
            {
                newService(domainCatchallHttpImap),
                "imaps://imap-server-01.vt.edu/",
                true,
            },
            // Global catch-all for HTTP(S)/IMAP(S) #1
            {
                newService(globalCatchallHttpImap),
                "https://host-01.example.com/",
                true,
            },
            // Global catch-all for HTTP(S)/IMAP(S) #2
            {
                newService(globalCatchallHttpImap),
                "imap://host-02.example.edu/",
                true,
            },
            // Null case
            {
                newService(globalCatchallHttpImap),
                null,
                false,
            },
        });
    }

    private static RegexRegisteredService newService(final String id) {
        val service = new RegexRegisteredService();
        service.setServiceId(id);
        return service;
    }

    @Test
    public void verifyMatches() {
        val testService = serviceToMatch == null ? null : RegisteredServiceTestUtils.getService(serviceToMatch);
        assertEquals(expected, service.matches(testService));
    }

    @Test
    public void verifySerializeARegexRegisteredServiceToJson() throws IOException {
        val serviceWritten = newService("serviceId");
        serviceWritten.setLogoutType(RegisteredServiceLogoutType.FRONT_CHANNEL);
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        val serviceRead = MAPPER.readValue(JSON_FILE, RegexRegisteredService.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
