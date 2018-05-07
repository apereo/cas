package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.util.MockWebServer;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link RestfulServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RestfulServiceRegistryTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySave() throws Exception {
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService();
        final String data = MAPPER.writeValueAsString(service);

        try (MockWebServer webServer = new MockWebServer(9295,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            final RestfulServiceRegistry s = new RestfulServiceRegistry(new RestTemplate(), "http://localhost:9295", new LinkedMultiValueMap<>());
            assertNotNull(s.save(service));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyLoad() throws Exception {
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService();
        final String data = MAPPER.writeValueAsString(new RegisteredService[]{service});

        try (MockWebServer webServer = new MockWebServer(9295,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            final RestfulServiceRegistry s = new RestfulServiceRegistry(new RestTemplate(), "http://localhost:9295", new LinkedMultiValueMap<>());
            assertTrue(s.size() == 1);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyDelete() {
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService();
        final String data = "200";

        try (MockWebServer webServer = new MockWebServer(9295,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            final RestfulServiceRegistry s = new RestfulServiceRegistry(new RestTemplate(), "http://localhost:9295", new LinkedMultiValueMap<>());
            assertTrue(s.delete(service));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyFind() throws Exception {
        final RegisteredService service = RegisteredServiceTestUtils.getRegisteredService();
        final String data = MAPPER.writeValueAsString(service);

        try (MockWebServer webServer = new MockWebServer(9295,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            final RestfulServiceRegistry s = new RestfulServiceRegistry(new RestTemplate(), "http://localhost:9295", new LinkedMultiValueMap<>());
            assertNotNull(s.findServiceById(service.getId()));
            assertNotNull(s.findServiceById(service.getServiceId()));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
