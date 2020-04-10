package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;

import java.util.Optional;

/**
 * This is {@link RestfulSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class RestfulSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public RestfulSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        super(samlIdPMetadataGeneratorConfigurationContext);
    }

    private static String getAppliesToFor(final Optional<SamlRegisteredService> result) {
        if (result.isPresent()) {
            val registeredService = result.get();
            return registeredService.getName() + '-' + registeredService.getId();
        }
        return "CAS";
    }

    @Override
    public void afterPropertiesSet() {
        generate(Optional.empty());
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) {
        doc.setAppliesTo(getAppliesToFor(registeredService));
        val properties = getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getMetadata().getRest();
        val url = StringUtils.appendIfMissing(properties.getUrl(), "/").concat("idp");
        HttpResponse response = null;
        try {
            response = HttpUtils.executePost(url, properties.getBasicAuthUsername(),
                properties.getBasicAuthPassword(), MAPPER.writeValueAsString(doc));
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    return doc;
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }
}
