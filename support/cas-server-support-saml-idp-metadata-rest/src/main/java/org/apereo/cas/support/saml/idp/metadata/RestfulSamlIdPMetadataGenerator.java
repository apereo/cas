package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.Optional;

/**
 * This is {@link RestfulSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class RestfulSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public RestfulSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        super(samlIdPMetadataGeneratorConfigurationContext);
    }

    @Override
    public void afterPropertiesSet() {
        FunctionUtils.doUnchecked(__ -> generate(Optional.empty()));
    }

    @Override
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }

    @Override
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) throws Exception {
        doc.setAppliesTo(getAppliesToFor(registeredService));
        val properties = getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getMetadata().getRest();
        val url = Strings.CI.appendIfMissing(properties.getUrl(), "/").concat("idp");
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(url)
                .headers(headers)
                .entity(MAPPER.writeValueAsString(doc))
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                if (status.is2xxSuccessful()) {
                    return doc;
                }
            }
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }
}
