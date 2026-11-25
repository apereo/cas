package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;
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
    void verifyServiceIdentifierTooLarge() throws Throwable {
        mockMvc.perform(get("/actuator/registeredServices/{id}", "99999999999999999999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyValidateService() throws Throwable {
        var content = new RegisteredServiceJsonSerializer(applicationContext)
            .toString(RegisteredServiceTestUtils.getRegisteredService());
        mockMvc.perform(post("/actuator/registeredServices/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isOk());

        content = new RegisteredServiceYamlSerializer(applicationContext)
            .toString(RegisteredServiceTestUtils.getRegisteredService());
        mockMvc.perform(post("/actuator/registeredServices/validate")
                .contentType(MediaType.APPLICATION_YAML_VALUE)
                .content(content))
            .andExpect(status().isOk());
    }
    
    @Test
    void verifyUnreadableServiceIsRefused() throws Throwable {
        val content = """
            {
              "@class": "org.apereo.cas.services.CasRegisteredService",
              "serviceId": "^https://mfa.example.org/.*",
              "name": "UnreadableService",
              "id": 9191,
              "multifactorPolicy": {
                "principalAttributeNameTrigger": "gender",
                "bypassPrincipalAttributeName": "gender"
              }
            }
            """;
        for (val request : List.of(
            post("/actuator/registeredServices/validate"),
            post("/actuator/registeredServices"),
            put("/actuator/registeredServices"))) {
            val body = mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
            assertTrue(body.contains("multifactorPolicy"), body);
        }
        assertNull(servicesManager.findServiceBy(9191));
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
                .contentType(MediaType.APPLICATION_YAML)
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
    void verifyExportSelectedServices() throws Throwable {
        val first = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(first);
        val second = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(second);
        val others = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(others);

        val content = mockMvc.perform(get("/actuator/registeredServices/export")
                .param("ids", String.valueOf(first.getId()), String.valueOf(second.getId()), String.valueOf(Long.MAX_VALUE))
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .andExpect(status().isOk())
            .andExpect(header().string("Filename", "services.zip"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
        val names = new ArrayList<String>();
        try (val zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                names.add(entry.getName());
            }
        }
        assertEquals(2, names.size(), names::toString);
        assertTrue(names.stream().anyMatch(name -> name.contains(first.getName())));
        assertTrue(names.stream().anyMatch(name -> name.contains(second.getName())));
        assertTrue(names.stream().noneMatch(name -> name.contains(others.getName())));

        mockMvc.perform(get("/actuator/registeredServices/export")
                .param("ids", String.valueOf(Long.MAX_VALUE))
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    void verifyIncompleteServiceIsRefused() throws Throwable {
        val incomplete = """
            {
              "@class": "org.apereo.cas.services.CasRegisteredService",
              "description": "No name and no service id"
            }
            """;
        mockMvc.perform(post("/actuator/registeredServices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incomplete))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("The definition has no name and no serviceId"));
        mockMvc.perform(put("/actuator/registeredServices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incomplete))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/actuator/registeredServices/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incomplete.replace("\"description\"", "\"name\": \"Named\", \"description\"")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("The definition has no serviceId"));
    }

    @Test
    void verifyExportUnknownService() throws Throwable {
        mockMvc.perform(get("/actuator/registeredServices/export/{id}", Long.MAX_VALUE)
                .accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .andExpect(status().isNotFound());
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(1))
                .andExpect(jsonPath("$.skipped").value(0));
        }
    }

    @Test
    void verifyBulkImportSavesEachServiceOnce() throws Throwable {
        val prefix = "Import-" + UUID.randomUUID();
        val saved = new ConcurrentHashMap<String, Integer>();
        applicationContext.addApplicationListener((ApplicationListener<CasRegisteredServiceSavedEvent>) event -> {
            val name = event.getRegisteredService().getName();
            if (name != null && name.startsWith(prefix)) {
                saved.merge(name, 1, Integer::sum);
            }
        });
        try (val out = new ByteArrayOutputStream(4096);
             val zipStream = new ZipOutputStream(out)) {
            val serializer = new RegisteredServiceJsonSerializer(applicationContext);
            for (var index = 0; index < 3; index++) {
                val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
                registeredService.setId(RandomUtils.nextLong());
                registeredService.setName(prefix + '-' + index);
                zipStream.putNextEntry(new ZipEntry(registeredService.getName() + ".json"));
                zipStream.write(serializer.toString(registeredService).getBytes(StandardCharsets.UTF_8));
                zipStream.closeEntry();
            }
            zipStream.finish();
            mockMvc.perform(post("/actuator/registeredServices/import")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(out.toByteArray()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(3))
                .andExpect(jsonPath("$.skipped").value(0));
        }
        assertEquals(Map.of(prefix + "-0", 1, prefix + "-1", 1, prefix + "-2", 1), saved);
    }

    @Test
    void verifyBulkImportReportsWhatWasImported() throws Throwable {
        try (val out = new ByteArrayOutputStream(4096);
             val zipStream = new ZipOutputStream(out)) {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService.setId(RandomUtils.nextLong());
            zipStream.putNextEntry(new ZipEntry("Readable.json"));
            zipStream.write(new RegisteredServiceJsonSerializer(applicationContext).toString(registeredService).getBytes(StandardCharsets.UTF_8));
            zipStream.closeEntry();
            zipStream.putNextEntry(new ZipEntry("Broken.json"));
            zipStream.write("{ not a definition".getBytes(StandardCharsets.UTF_8));
            zipStream.closeEntry();
            zipStream.putNextEntry(new ZipEntry("Notes.txt"));
            zipStream.write("ignored".getBytes(StandardCharsets.UTF_8));
            zipStream.closeEntry();
            zipStream.finish();
            mockMvc.perform(post("/actuator/registeredServices/import")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(out.toByteArray()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(1))
                .andExpect(jsonPath("$.skipped").value(1));
        }
    }

    @Test
    void verifyBulkImportWithNothingReadable() throws Throwable {
        try (val out = new ByteArrayOutputStream(1024);
             val zipStream = new ZipOutputStream(out)) {
            zipStream.putNextEntry(new ZipEntry("Broken.json"));
            zipStream.write("{ not a definition".getBytes(StandardCharsets.UTF_8));
            zipStream.closeEntry();
            zipStream.finish();
            mockMvc.perform(post("/actuator/registeredServices/import")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(out.toByteArray()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.imported").value(0))
                .andExpect(jsonPath("$.skipped").value(1));
        }
    }

    @Test
    void verifyBulkImportRefusesOversizedDefinition() throws Throwable {
        try (val out = new ByteArrayOutputStream(4096);
             val zipStream = new ZipOutputStream(out)) {
            zipStream.putNextEntry(new ZipEntry("Large.json"));
            zipStream.write(new byte[1024 * 1024 + 1]);
            zipStream.closeEntry();
            zipStream.finish();
            mockMvc.perform(post("/actuator/registeredServices/import")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(out.toByteArray()))
                .andExpect(status().isContentTooLarge());
        }
    }

    /*
     * Deleting all services wipes every definition in the services manager, so it runs in its own
     * application context and cannot remove services that concurrent tests in this class rely on.
     */
    @Nested
    @TestPropertySource(properties = "spring.application.name=${random.uuid}")
    class DeleteAllTests extends AbstractCasEndpointTests {
        @Test
        void verifyOperation() throws Throwable {
            mockMvc.perform(delete("/actuator/registeredServices/cache")
                .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());
            mockMvc.perform(delete("/actuator/registeredServices")
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
    }
}
