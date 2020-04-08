package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.support.saml.idp.metadata.RestSamlMetadataProperties;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
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
public class RestfulSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final RestSamlMetadataProperties properties;

    public RestfulSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                         final RestSamlMetadataProperties properties) {
        super(metadataCipherExecutor);
        this.properties = properties;
    }

    private static String getAppliesToFor(final Optional<SamlRegisteredService> result) {
        if (result.isPresent()) {
            val registeredService = result.get();
            return registeredService.getName() + '-' + registeredService.getId();
        }
        return "CAS";
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        val url = StringUtils.appendIfMissing(properties.getUrl(), "/").concat("idp");
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            registeredService.ifPresent(service -> parameters.put("appliesTo", getAppliesToFor(registeredService)));
            response = HttpUtils.executeGet(url, properties.getBasicAuthUsername(),
                properties.getBasicAuthPassword(), parameters);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val entity = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    val document = MAPPER.readValue(JsonValue.readHjson(entity).toString(), SamlIdPMetadataDocument.class);
                    if (document != null && document.isValid()) {
                        return document;
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }
}
