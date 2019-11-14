package org.apereo.cas.support.saml.web.idp.metadata;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * The {@link SamlIdPMetadataController} will attempt
 * to produce saml metadata for CAS as an identity provider.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Controller("samlIdPMetadataController")
@Slf4j
@RequiredArgsConstructor
public class SamlIdPMetadataController implements InitializingBean {
    private static final String CONTENT_TYPE = "text/xml;charset=UTF-8";

    private final SamlIdPMetadataGenerator metadataAndCertificatesGenerationService;
    private final SamlIdPMetadataLocator samlIdPMetadataLocator;
    private final ServicesManager servicesManager;
    
    @Override
    public void afterPropertiesSet() {
        this.metadataAndCertificatesGenerationService.generate(Optional.empty());
    }

    /**
     * Displays the identity provider metadata.
     * Checks to make sure metadata exists, and if not, generates it first.
     *
     * @param service  the service
     * @param response servlet response
     * @throws IOException the IO exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_IDP_METADATA)
    public void generateMetadataForIdp(@RequestParam(value = "service", required = false) final String service,
                                       final HttpServletResponse response) throws IOException {

        val registeredService = getRegisteredServiceIfAny(service);
        this.metadataAndCertificatesGenerationService.generate(registeredService);
        val md = this.samlIdPMetadataLocator.resolveMetadata(registeredService).getInputStream();
        val contents = IOUtils.toString(md, StandardCharsets.UTF_8);
        response.setContentType(CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_OK);
        try (val writer = response.getWriter()) {
            LOGGER.debug("Producing metadata for the response");
            writer.write(contents);
            writer.flush();
        }
    }

    private Optional<SamlRegisteredService> getRegisteredServiceIfAny(final String service) {
        if (NumberUtils.isDigits(service)) {
            val svc = this.servicesManager.findServiceBy(Long.parseLong(service), SamlRegisteredService.class);
            return Optional.ofNullable(svc);
        }
        val svc = this.servicesManager.findServiceBy(service, SamlRegisteredService.class);
        return Optional.ofNullable(svc);
    }
}
