package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.Service;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

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
        final String domainCatchallHttp = "https*://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        final String domainCatchallHttpImap = "(https*|imaps*)://([A-Za-z0-9_-]+\\.)+vt\\.edu/.*";
        final String globalCatchallHttpImap = "(https*|imaps*)://.*";
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

    @Test
    public void verifyMatches() {
        final Service testService;
        if (serviceToMatch == null) {
            testService = null;
        } else {
            testService = RegisteredServiceTestUtils.getService(serviceToMatch);
        }
        assertEquals(expected, service.matches(testService));
    }

    private static RegexRegisteredService newService(final String id) {
        final RegexRegisteredService service = new RegexRegisteredService();
        service.setServiceId(id);
        return service;
    }

    @Test
    public void verifySerializeARegexRegisteredServiceToJson() throws IOException {
        final RegexRegisteredService serviceWritten = newService("serviceId");
        serviceWritten.setLogoutType(RegisteredService.LogoutType.FRONT_CHANNEL);
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        final RegisteredService serviceRead = MAPPER.readValue(JSON_FILE, RegexRegisteredService.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
