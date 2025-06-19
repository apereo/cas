package org.apereo.cas.web.report;

import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.apereo.cas.web.CasYamlHttpMessageConverter.MEDIA_TYPE_CAS_YAML;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link RegisteredServicesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = "management.endpoint.registeredServices.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class RegisteredServicesEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyOperation() throws Throwable {
        mockMvc.perform(delete("/actuator/registeredServices/cache")
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        val service1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val service2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service1, service2);

        mockMvc.perform(get("/actuator/registeredServices")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));

        mockMvc.perform(get("/actuator/registeredServices/type/{type}", CasRegisteredService.class.getSimpleName())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));

        mockMvc.perform(get("/actuator/registeredServices/{id}", service1.getServiceId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThan(0)));

        mockMvc.perform(delete("/actuator/registeredServices/{id}", service1.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)    
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/actuator/registeredServices/{id}", service1.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/actuator/registeredServices/{id}", service2.getServiceId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/actuator/registeredServices/{id}", service2.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyImportOperationAsJson() throws Throwable {
        val content = new RegisteredServiceJsonSerializer(applicationContext)
            .toString(RegisteredServiceTestUtils.getRegisteredService());
        mockMvc.perform(post("/actuator/registeredServices/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isCreated());
    }

    @Test
    void verifyImportOperationFails() throws Throwable {
        mockMvc.perform(post("/actuator/registeredServices/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(StringUtils.EMPTY))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifySaveService() throws Exception {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val content = new RegisteredServiceJsonSerializer(applicationContext).toString(service);
        mockMvc.perform(post("/actuator/registeredServices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isOk());
    }

    @Test
    void verifyImportOperationAsYaml() throws Throwable {
        val content = new RegisteredServiceYamlSerializer(applicationContext)
            .toString(RegisteredServiceTestUtils.getRegisteredService());
        mockMvc.perform(post("/actuator/registeredServices/import")
                .contentType(MEDIA_TYPE_CAS_YAML)
                .content(content))
            .andExpect(status().isCreated());
    }

    @Test
    void verifyExportOperation() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service);
        mockMvc.perform(get("/actuator/registeredServices/export")
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/registeredServices/export/{id}", service.getId())
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .andExpect(status().isOk());
    }


    @Test
    void verifyServiceUpdate() throws Exception {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service);
        val content = new RegisteredServiceJsonSerializer(applicationContext).toString(service);
        mockMvc.perform(put("/actuator/registeredServices")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(content)
            )
            .andExpect(status().isOk());
    }


    @Test
    void verifyBulkImportAsZip() throws Throwable {
        try (val out = new ByteArrayOutputStream(2048);
             val zipStream = new ZipOutputStream(out)) {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            val content = new RegisteredServiceJsonSerializer(applicationContext).toString(registeredService);
            val name = registeredService.getName() + ".json";
            val zipEntry = new ZipEntry(name);
            zipStream.putNextEntry(zipEntry);

            val data = content.getBytes(StandardCharsets.UTF_8);
            zipStream.write(data, 0, data.length);
            zipStream.closeEntry();

            mockMvc.perform(post("/actuator/registeredServices/import")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(out.toByteArray())
                )
                .andExpect(status().isOk());
        }
    }
}

