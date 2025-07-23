package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.support.saml.idp.metadata.RestSamlMetadataProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link RestfulSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@Monitorable
public class RestfulSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final RestSamlMetadataProperties properties;

    public RestfulSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                         final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                         final RestSamlMetadataProperties properties,
                                         final ConfigurableApplicationContext applicationContext) {
        super(metadataCipherExecutor, metadataCache, applicationContext);
        this.properties = properties;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        val url = Strings.CI.appendIfMissing(properties.getUrl(), "/").concat("idp");
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, String>();
            registeredService.ifPresent(service -> parameters.put("appliesTo", getAppliesToFor(registeredService)));
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(url)
                .parameters(parameters)
                .headers(properties.getHeaders())
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                if (status.is2xxSuccessful()) {
                    try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val entity = IOUtils.toString(content, StandardCharsets.UTF_8);
                        val document = MAPPER.readValue(JsonValue.readHjson(entity).toString(), SamlIdPMetadataDocument.class);
                        if (document != null && document.isValid()) {
                            return document;
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }
}
