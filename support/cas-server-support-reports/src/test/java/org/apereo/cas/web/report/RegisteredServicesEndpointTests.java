package org.apereo.cas.web.report;

import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServicesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = "management.endpoint.registeredServices.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class RegisteredServicesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("registeredServicesReportEndpoint")
    private RegisteredServicesEndpoint endpoint;

    @Test
    void verifyOperation() throws Throwable {
        endpoint.deleteCache();
        
        val service1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val service2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service1, service2);

        assertNotNull(endpoint.handle().getBody());
        assertNotNull(endpoint.fetchServicesByType(CasRegisteredService.class.getSimpleName()).getBody());
        assertNotNull(endpoint.fetchService(service1.getServiceId()).getBody());
        assertNotNull(endpoint.deleteService(service1.getServiceId()).getBody());
        assertEquals(HttpStatus.NOT_FOUND, endpoint.fetchService(String.valueOf(service1.getId())).getStatusCode());

        assertNotNull(endpoint.deleteService(String.valueOf(service2.getId())).getBody());
        assertEquals(HttpStatus.NOT_FOUND, endpoint.deleteService(String.valueOf(service2.getId())).getStatusCode());
    }

    @Test
    void verifyImportOperationAsJson() throws Throwable {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val request = new MockHttpServletRequest();
        val content = new RegisteredServiceJsonSerializer(appCtx).toString(RegisteredServiceTestUtils.getRegisteredService());
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, endpoint.importService(request).getStatusCode());
    }

    @Test
    void verifyImportOperationFails() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setContent(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.BAD_REQUEST, endpoint.importService(request).getStatusCode());
    }

    @Test
    void verifySaveService() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val content = new RegisteredServiceJsonSerializer(appCtx).toString(service);
        assertEquals(HttpStatus.OK, endpoint.saveService(content).getStatusCode());
    }

    @Test
    void verifyImportOperationAsYaml() throws Throwable {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val request = new MockHttpServletRequest();
        val content = new RegisteredServiceYamlSerializer(appCtx)
            .toString(RegisteredServiceTestUtils.getRegisteredService());
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, endpoint.importService(request).getStatusCode());
    }

    @Test
    void verifyExportOperation() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        this.servicesManager.save(service);
        val response = endpoint.export();
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(endpoint.export(service.getId()));
    }

    @Test
    void verifyServiceUpdate() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service);
        val content = new RegisteredServiceJsonSerializer(appCtx).toString(service);
        val response = endpoint.updateService(content);
        assertNotNull(response);
    }

    @Test
    void verifyBulkImportAsZip() throws Throwable {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        try (val out = new ByteArrayOutputStream(2048);
             val zipStream = new ZipOutputStream(out)) {
            var registeredService = RegisteredServiceTestUtils.getRegisteredService();
            val content = new RegisteredServiceJsonSerializer(appCtx).toString(registeredService);
            var name = registeredService.getName() + ".json";
            val e = new ZipEntry(name);
            zipStream.putNextEntry(e);

            val data = content.getBytes(StandardCharsets.UTF_8);
            zipStream.write(data, 0, data.length);
            zipStream.closeEntry();
            request.setContent(out.toByteArray());
        }
        assertEquals(HttpStatus.OK, endpoint.importService(request).getStatusCode());
    }
}

