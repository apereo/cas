package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apereo.inspektr.audit.annotation.Audit;
import org.hjson.JsonValue;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link RestfulSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RestfulSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver
    implements SamlRegisteredServiceMetadataManager {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public RestfulSamlRegisteredServiceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                                        final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        HttpResponse response = null;
        try {
            val rest = samlIdPProperties.getMetadata().getRest();
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.valueOf(rest.getMethod().toUpperCase(Locale.ENGLISH).trim()))
                .url(rest.getUrl())
                .parameters(CollectionUtils.wrap("entityId", service.getServiceId()))
                .maximumRetryAttempts(rest.getMaximumRetryAttempts())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && HttpStatus.resolve(response.getCode()).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    val doc = MAPPER.readValue(JsonValue.readHjson(result).toString(), SamlMetadataDocument.class);
                    val resolver = buildMetadataResolverFrom(service, doc);
                    return CollectionUtils.wrapList(resolver);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith(getSourceId());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        if (supports(service)) {
            val rest = samlIdPProperties.getMetadata().getRest();
            val status = HttpRequestUtils.pingUrl(rest.getUrl());
            return !status.isError();
        }
        return false;
    }

    @Override
    public String getSourceId() {
        return "rest://";
    }

    @Override
    public SamlMetadataDocument store(final SamlMetadataDocument document) {
        HttpResponse response = null;
        try {
            val rest = samlIdPProperties.getMetadata().getRest();
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(rest.getUrl())
                .maximumRetryAttempts(rest.getMaximumRetryAttempts())
                .headers(headers)
                .entity(MAPPER.writeValueAsString(document))
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                LOGGER.debug("Uploaded etadata document [{}]. Status: [{}]", document.getName(), response.getCode());
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return document;
    }

    @Override
    public List<SamlMetadataDocument> load() {
        HttpResponse response = null;
        try {
            val rest = samlIdPProperties.getMetadata().getRest();
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .maximumRetryAttempts(rest.getMaximumRetryAttempts())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && HttpStatus.resolve(response.getCode()).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    return MAPPER.readValue(JsonValue.readHjson(result).toString(), new TypeReference<>() {
                    });
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return List.of();
    }

    @Override
    public void removeById(final long id) {
        HttpResponse response = null;
        try {
            val rest = samlIdPProperties.getMetadata().getRest();
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(rest.getUrl())
                .parameters(CollectionUtils.wrap("id", String.valueOf(id)))
                .maximumRetryAttempts(rest.getMaximumRetryAttempts())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void removeAll() {
        HttpResponse response = null;
        try {
            val rest = samlIdPProperties.getMetadata().getRest();
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(rest.getUrl())
                .maximumRetryAttempts(rest.getMaximumRetryAttempts())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void removeByName(final String name) {
        HttpResponse response = null;
        try {
            val rest = samlIdPProperties.getMetadata().getRest();
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(rest.getUrl())
                .parameters(CollectionUtils.wrap("name", name))
                .maximumRetryAttempts(rest.getMaximumRetryAttempts())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public Optional<SamlMetadataDocument> findByName(final String name) {
        HttpResponse response = null;
        try {
            val rest = samlIdPProperties.getMetadata().getRest();
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .parameters(CollectionUtils.wrap("name", name))
                .maximumRetryAttempts(rest.getMaximumRetryAttempts())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && HttpStatus.resolve(response.getCode()).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    return Optional.of(MAPPER.readValue(JsonValue.readHjson(result).toString(), SamlMetadataDocument.class));
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }

    @Override
    public Optional<SamlMetadataDocument> findById(final long id) {
        HttpResponse response = null;
        try {
            val rest = samlIdPProperties.getMetadata().getRest();
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .parameters(CollectionUtils.wrap("id", String.valueOf(id)))
                .maximumRetryAttempts(rest.getMaximumRetryAttempts())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && HttpStatus.resolve(response.getCode()).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    return Optional.of(MAPPER.readValue(JsonValue.readHjson(result).toString(), SamlMetadataDocument.class));
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }
}
