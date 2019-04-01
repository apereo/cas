package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.hjson.JsonValue;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * This is {@link JsonResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class JsonResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private Map<String, SamlServiceProviderMetadata> metadataMap;

    private final String metadataTemplate;
    private final Resource jsonResource;

    public JsonResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                        final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);

        try {
            this.metadataTemplate = IOUtils.toString(new ClassPathResource("metadata/sp-metadata-template.xml").getInputStream(), StandardCharsets.UTF_8);
            val md = samlIdPProperties.getMetadata();
            this.jsonResource = new FileSystemResource(new File(md.getLocation().getFile(), "saml-sp-metadata.json"));
            if (this.jsonResource.exists()) {
                this.metadataMap = readDecisionsFromJsonResource();
                val watcher = new FileWatcherService(jsonResource.getFile(), file -> this.metadataMap = readDecisionsFromJsonResource());
                watcher.start(getClass().getSimpleName());
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        try {
            if (metadataMap.containsKey(service.getServiceId())) {
                val sp = metadataMap.get(service.getServiceId());
                val metadata = metadataTemplate
                    .replace("${entityId}", sp.getEntityId())
                    .replace("${certificate}", sp.getCertificate())
                    .replace("${assertionConsumerServiceUrl}", sp.getAssertionConsumerServiceUrl());
                val metadataResource = new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8));
                val resolver = new InMemoryResourceMetadataResolver(metadataResource, configBean);
                configureAndInitializeSingleMetadataResolver(resolver, service);
                return CollectionUtils.wrap(resolver);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith("json://");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return ResourceUtils.doesResourceExist(this.jsonResource);
    }

    @SneakyThrows
    private Map<String, SamlServiceProviderMetadata> readDecisionsFromJsonResource() {
        try (val reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
            final TypeReference<Map<String, SamlServiceProviderMetadata>> personList = new TypeReference<>() {
            };
            return MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
        }
    }


    /**
     * The Saml service provider metadata.
     */
    @Getter
    @Setter
    public static class SamlServiceProviderMetadata implements Serializable {
        private static final long serialVersionUID = -7347473226470492601L;

        private String entityId;
        private String certificate;
        private String assertionConsumerServiceUrl;
    }
}

