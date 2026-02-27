package org.apereo.cas.support.saml.web.idp.metadata;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This is {@link SamlRegisteredServiceMetadataEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@Endpoint(id = "samlIdPRegisteredServiceMetadata", defaultAccess = Access.NONE)
public class SamlRegisteredServiceMetadataEndpoint extends BaseCasRestActuatorEndpoint {
    public SamlRegisteredServiceMetadataEndpoint(final CasConfigurationProperties casProperties,
                                                 final ConfigurableApplicationContext applicationContext) {
        super(casProperties, applicationContext);
    }

    /**
     * Gets metadata managers.
     *
     * @return the metadata managers
     */
    @GetMapping(
        path = "/managers",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Get all registered SAML2 metadata managers and their details")
    public List getMetadataManagers() {
        val managers = applicationContext.getBeansOfType(SamlRegisteredServiceMetadataManager.class).values();
        return managers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(manager -> new MetadataManagerEntity(
                manager.getName(),
                manager.getSourceId()))
            .toList();
    }


    /**
     * Gets metadata entries by manager.
     *
     * @param name the name
     * @return the metadata entries by manager
     */
    @GetMapping(
        path = "/managers/{name}",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Get all registered SAML2 metadata entries for this manager",
        parameters = @Parameter(
            name = "name",
            in = ParameterIn.PATH,
            required = true,
            description = "The metadata manager name"
        ))
    public ResponseEntity<List<SamlMetadataDocument>> getMetadataEntriesByManager(@PathVariable final String name) {
        val managers = applicationContext.getBeansOfType(SamlRegisteredServiceMetadataManager.class);
        return managers
            .values()
            .stream()
            .filter(manager -> manager.getName().equals(name))
            .findFirst()
            .map(manager -> ResponseEntity.ok(manager.load()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Gets metadata entry by id for manager.
     *
     * @param name the name
     * @param id   the id
     * @return the metadata entry by id for manager
     */
    @GetMapping(
        path = "/managers/{name}/{id}",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Get registered SAML2 metadata entry for this manager",
        parameters = {
            @Parameter(
                name = "name",
                in = ParameterIn.PATH,
                required = true,
                description = "The metadata manager name"
            ),
            @Parameter(
                name = "id",
                in = ParameterIn.PATH,
                required = true,
                description = "The metadata entry id"
            )
        }
    )
    public ResponseEntity<SamlMetadataDocument> getMetadataEntryByIdForManager(
        @PathVariable final String name, @PathVariable final long id) {
        val managers = applicationContext.getBeansOfType(SamlRegisteredServiceMetadataManager.class);
        return managers
            .values()
            .stream()
            .filter(manager -> manager.getName().equals(name))
            .findFirst()
            .map(manager -> ResponseEntity.of(manager.findById(id)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Remove metadata entry by id for manager.
     *
     * @param name the name
     * @param id   the id
     * @return the response entity
     */
    @DeleteMapping(
        path = "/managers/{name}/{id}",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Remove registered SAML2 metadata entry for this manager",
        parameters = {
            @Parameter(
                name = "name",
                in = ParameterIn.PATH,
                required = true,
                description = "The metadata manager name"
            ),
            @Parameter(
                name = "id",
                in = ParameterIn.PATH,
                required = true,
                description = "The metadata entry id"
            )
        }
    )
    public ResponseEntity removeMetadataEntryByIdForManager(
        @PathVariable final String name, @PathVariable final long id) {
        val managers = applicationContext.getBeansOfType(SamlRegisteredServiceMetadataManager.class);
        return managers
            .values()
            .stream()
            .filter(manager -> manager.getName().equals(name))
            .findFirst()
            .map(manager -> {
                manager.removeById(id);
                return ResponseEntity.noContent().build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Upload metadata response entity.
     *
     * @param name     the name
     * @param document the document
     * @return the response entity
     */
    @RequestMapping(
        path = "/managers/{name}",
        method = {RequestMethod.POST, RequestMethod.PUT},
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Upload SAML2 metadata document to a metadata manager identified by its name",
        parameters = @Parameter(
            name = "name",
            in = ParameterIn.PATH,
            required = true,
            description = "The metadata manager name"
        ),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            description = "The SAML2 metadata document to upload",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SamlMetadataDocument.class))))
    public ResponseEntity uploadMetadata(@PathVariable final String name, @RequestBody final SamlMetadataDocument document) {
        val managers = applicationContext.getBeansOfType(SamlRegisteredServiceMetadataManager.class);
        return managers
            .values()
            .stream()
            .filter(manager -> manager.getName().equals(name))
            .findFirst()
            .map(manager -> ResponseEntity.ok(manager.store(document)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private record MetadataManagerEntity(String name, String sourceId) {
    }

}
