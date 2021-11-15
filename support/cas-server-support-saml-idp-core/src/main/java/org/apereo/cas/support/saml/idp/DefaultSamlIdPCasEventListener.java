package org.apereo.cas.support.saml.idp;

import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.Optional;

/**
 * This is {@link DefaultSamlIdPCasEventListener}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultSamlIdPCasEventListener implements SamlIdPCasEventListener {
    private final SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Override
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        val document = FunctionUtils.doAndHandle(() -> {
            LOGGER.debug("Attempting to generate/fetch SAML IdP metadata...");
            return samlIdPMetadataGenerator.generate(Optional.empty());
        }, e -> {
            LoggingUtils.error(LOGGER, e);
            return null;
        }).get();
        LOGGER.trace("Generated SAML IdP metadata document is [{}]", document);
    }
}
