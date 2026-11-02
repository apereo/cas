package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.hjson.JsonValue;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link JsonResourceMetadataResolver}.
 * <p>
 * Service providers are read from {@code saml-sp-metadata.json} in the metadata directory. The file is read again
 * whenever its modification time changes, including when it appears after startup, and it is reported as a
 * metadata source file so that cached metadata built from it is reloaded once it changes.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class JsonResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final String metadataTemplate;

    private final File jsonFile;

    private final Lock lock = new ReentrantLock();

    private volatile Map<String, SamlServiceProviderMetadata> metadataMap = Map.of();

    private volatile long metadataLastModified = -1;

    public JsonResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                        final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
        this.metadataTemplate = FunctionUtils.doUnchecked(() -> {
            val inputStream = new ClassPathResource("metadata/sp-metadata-template.xml").getInputStream();
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        });
        val md = samlIdPProperties.getMetadata();
        val location = SpringExpressionLanguageValueResolver.getInstance().resolve(md.getFileSystem().getLocation());
        val metadataDir = FunctionUtils.doUnchecked(() -> ResourceUtils.getRawResourceFrom(location).getFile());

        this.jsonFile = new File(metadataDir, "saml-sp-metadata.json");
        LOGGER.debug("Service provider metadata as JSON may be found at [{}]", jsonFile);
        getMetadataMap();
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        val metadata = getMetadataMap();
        if (metadata.containsKey(service.getServiceId())) {
            return FunctionUtils.doUnchecked(() -> {
                val sp = metadata.get(service.getServiceId());
                val document = metadataTemplate
                    .replace("${entityId}", sp.getEntityId())
                    .replace("${certificate}", sp.getCertificate())
                    .replace("${binding}", sp.getBinding())
                    .replace("${assertionConsumerServiceUrl}", sp.getAssertionConsumerServiceUrl());
                val metadataResource = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8));
                val resolver = new InMemoryResourceMetadataResolver(metadataResource, configBean);
                configureAndInitializeSingleMetadataResolver(resolver, service);
                return CollectionUtils.wrap(resolver);
            });
        }
        return new ArrayList<>();
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        val metadataLocation = Objects.requireNonNull(service).getMetadataLocation();
        return metadataLocation.trim().startsWith(getSourceId());
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return jsonFile.isFile();
    }

    @Override
    public Collection<File> getMetadataSourceFiles(final SamlRegisteredService service) {
        return supports(service) ? List.of(jsonFile) : List.of();
    }

    /**
     * Gets the service providers, reading the file again if its modification time has changed since it was last read.
     * A file that is removed yields no service providers.
     *
     * @return the service providers keyed by service id
     */
    protected Map<String, SamlServiceProviderMetadata> getMetadataMap() {
        val lastModified = jsonFile.isFile() ? jsonFile.lastModified() : -1L;
        if (lastModified != metadataLastModified) {
            lock.lock();
            try {
                if (lastModified != metadataLastModified) {
                    LOGGER.debug("Reading service provider metadata from [{}]", jsonFile);
                    metadataMap = lastModified < 0 ? Map.of() : readDecisionsFromJsonResource();
                    metadataLastModified = lastModified;
                }
            } finally {
                lock.unlock();
            }
        }
        return metadataMap;
    }

    private static String getSourceId() {
        return "json://";
    }

    /**
     * The Saml service provider metadata.
     */
    @Getter
    @Setter
    public static class SamlServiceProviderMetadata implements Serializable {
        @Serial
        private static final long serialVersionUID = -7347473226470492601L;

        private String entityId;

        private String certificate;

        private String assertionConsumerServiceUrl;

        private String binding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    }

    private Map<String, SamlServiceProviderMetadata> readDecisionsFromJsonResource() {
        return FunctionUtils.doUnchecked(() -> {
            try (val reader = Files.newBufferedReader(jsonFile.toPath(), StandardCharsets.UTF_8)) {
                val personList = new TypeReference<Map<String, SamlServiceProviderMetadata>>() {
                };
                return MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
            }
        });
    }
}

