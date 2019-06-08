package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.io.TemporaryFileSystemResource;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.core.io.Resource;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * This is {@link ExportRegisteredServicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint(id = "exportRegisteredServices", enableByDefault = false)
public class ExportRegisteredServicesEndpoint extends BaseCasActuatorEndpoint {
    private final ServicesManager servicesManager;

    /**
     * Instantiates a new mvc endpoint.
     * Endpoints are by default sensitive.
     *
     * @param casProperties   the cas properties
     * @param servicesManager the services manager
     */
    public ExportRegisteredServicesEndpoint(final CasConfigurationProperties casProperties, final ServicesManager servicesManager) {
        super(casProperties);
        this.servicesManager = servicesManager;
    }

    /**
     * Export services web endpoint response.
     *
     * @return the web endpoint response
     * @throws Exception the exception
     */
    @ReadOperation
    public WebEndpointResponse<Resource> exportServices() throws Exception {
        val date = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
        val file = File.createTempFile("services-" + date, ".zip");
        Files.deleteIfExists(file.toPath());

        val env = new HashMap<String, Object>();
        env.put("create", "true");
        env.put("encoding", StandardCharsets.UTF_8.name());
        try (val zipfs = FileSystems.newFileSystem(URI.create("jar:" + file.toURI().toString()), env)) {
            val serializer = new RegisteredServiceJsonSerializer();
            val services = this.servicesManager.load();
            services.forEach(Unchecked.consumer(service -> {
                val fileName = String.format("%s-%s", service.getName(), service.getId());
                val sourceFile = File.createTempFile(fileName, ".json");
                serializer.to(sourceFile, service);
                if (sourceFile.exists()) {
                    val pathInZipfile = zipfs.getPath("/".concat(sourceFile.getName()));
                    Files.copy(sourceFile.toPath(), pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
                }
            }));
        }
        val resource = new TemporaryFileSystemResource(file);
        return new WebEndpointResponse<>(resource);
    }
}
