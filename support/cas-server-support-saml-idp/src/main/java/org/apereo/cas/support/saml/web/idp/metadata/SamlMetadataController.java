package org.apereo.cas.support.saml.web.idp.metadata;

import org.apache.commons.io.FileUtils;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * The {@link SamlMetadataController} will attempt
 * to produce saml metadata for CAS as an identity provider.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Controller("samlMetadataController")
public class SamlMetadataController {

    private static final String CONTENT_TYPE = "text/xml;charset=UTF-8";

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlMetadataController.class);
    private final SamlIdpMetadataAndCertificatesGenerationService metadataAndCertificatesGenerationService;

    /**
     * Instantiates a new Saml metadata controller.
     * Required for bean initialization.
     *
     * @param metadataAndCertificatesGenerationService the metadata and certificates generation service
     */
    public SamlMetadataController(final SamlIdpMetadataAndCertificatesGenerationService metadataAndCertificatesGenerationService) {
        this.metadataAndCertificatesGenerationService = metadataAndCertificatesGenerationService;
    }

    /**
     * Post constructor placeholder for additional
     * extensions. This method is called after
     * the object has completely initialized itself.
     */
    @PostConstruct
    public void postConstruct() {
        this.metadataAndCertificatesGenerationService.performGenerationSteps();
    }

    /**
     * Displays the identity provider metadata.
     * Checks to make sure metadata exists, and if not, generates it first.
     *
     * @param response servlet response
     * @throws IOException the iO exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_IDP_METADATA)
    public void generateMetadataForIdp(final HttpServletResponse response) throws IOException {
        final File metadataFile = this.metadataAndCertificatesGenerationService.performGenerationSteps();
        final String contents = FileUtils.readFileToString(metadataFile, StandardCharsets.UTF_8);
        response.setContentType(CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter writer = response.getWriter()) {
            LOGGER.debug("Producing metadata for the response");
            writer.write(contents);
            writer.flush();
        }
    }
}
